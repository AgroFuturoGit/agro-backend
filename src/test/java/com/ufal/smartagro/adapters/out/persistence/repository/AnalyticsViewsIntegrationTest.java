package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AnalyticsViewsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Views analíticas e Materialized View devem existir com os índices corretos")
    void viewsAndMaterializedView_devemExistirNoBanco() {
        Integer activeFarmersViewCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.views WHERE table_name = 'vw_active_farmers'",
                Integer.class
        );
        assertThat(activeFarmersViewCount).isEqualTo(1);

        Integer planProgressViewCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.views WHERE table_name = 'vw_plan_progress'",
                Integer.class
        );
        assertThat(planProgressViewCount).isEqualTo(1);

        Integer mvCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_matviews WHERE matviewname = 'mv_org_harvest_summary'",
                Integer.class
        );
        assertThat(mvCount).isEqualTo(1);

        Integer uniqueIndexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'uq_mv_org_harvest_summary'",
                Integer.class
        );
        assertThat(uniqueIndexCount).isEqualTo(1);

        Integer orgHarvestIndexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE indexname = 'idx_mv_org_harvest_summary_org_harvest'",
                Integer.class
        );
        assertThat(orgHarvestIndexCount).isEqualTo(1);
    }

    @Test
    @DisplayName("vw_active_farmers deve incluir inadimplentes e filtrar agricultores com soft-delete")
    void vwActiveFarmers_incluiInadimplentesEExcluiSoftDeleted() {
        UUID orgId = insertOrganization("Org Teste Farmers");
        UUID commId = insertCommunity(orgId, "Comunidade Farmers");

        UUID activeCompliantFarmerId = insertFarmer(commId, "Ativo Adimplente", "11100011100", true, null);
        UUID activeNonCompliantFarmerId = insertFarmer(commId, "Ativo Inadimplente", "22200022200", false, null);
        UUID deletedFarmerId = insertFarmer(commId, "Excluído", "33300033300", true, java.sql.Timestamp.valueOf("2026-01-01 00:00:00"));

        List<Map<String, Object>> activeFarmers = jdbcTemplate.queryForList(
                "SELECT id, is_compliant FROM vw_active_farmers WHERE id IN (?, ?, ?)",
                activeCompliantFarmerId, activeNonCompliantFarmerId, deletedFarmerId
        );

        assertThat(activeFarmers).hasSize(2);

        Map<UUID, Boolean> complianceMap = activeFarmers.stream().collect(
                java.util.stream.Collectors.toMap(
                        row -> (UUID) row.get("id"),
                        row -> (Boolean) row.get("is_compliant")
                )
        );

        assertThat(complianceMap.get(activeCompliantFarmerId)).isTrue();
        assertThat(complianceMap.get(activeNonCompliantFarmerId)).isFalse();
        assertThat(complianceMap.containsKey(deletedFarmerId)).isFalse();
    }

    @Test
    @DisplayName("vw_plan_progress deve computar meta e agregar apenas apontamentos validados")
    void vwPlanProgress_agregaApenasApontamentosValidadosECalculaPercentual() {
        UUID orgId = insertOrganization("Org Teste Progress");
        UUID commId = insertCommunity(orgId, "Comunidade Progress");
        UUID farmerId = insertFarmer(commId, "Produtor Progresso", "44400044400", true, null);
        UUID harvestId = insertHarvest("Safra 2026");
        UUID cropId = insertCrop("Milho");

        // planted_area = 2.0, expected_productivity = 100.0 => target_kg = 200.0
        UUID planId = insertProductionPlan(farmerId, harvestId, cropId, new BigDecimal("2.0"), new BigDecimal("100.0"), "IN_PROGRESS", null);

        // 1 apontamento VALIDATED (50 kg)
        insertExecution(planId, new BigDecimal("50.0"), Date.valueOf(LocalDate.of(2026, 4, 10)), "VALIDATED", null);
        // 1 apontamento PENDING (30 kg) - não deve somar em realized_kg
        insertExecution(planId, new BigDecimal("30.0"), Date.valueOf(LocalDate.of(2026, 4, 15)), "PENDING", null);
        // 1 apontamento REJECTED (20 kg) - não deve somar em realized_kg
        insertExecution(planId, new BigDecimal("20.0"), Date.valueOf(LocalDate.of(2026, 4, 20)), "REJECTED", null);

        Map<String, Object> progress = jdbcTemplate.queryForMap(
                "SELECT plan_id, target_kg, realized_kg, completion_pct FROM vw_plan_progress WHERE plan_id = ?",
                planId
        );

        assertThat(new BigDecimal(progress.get("target_kg").toString())).isEqualByComparingTo("200.00");
        assertThat(new BigDecimal(progress.get("realized_kg").toString())).isEqualByComparingTo("50.00");
        assertThat(new BigDecimal(progress.get("completion_pct").toString())).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("mv_org_harvest_summary deve excluir planos cancelados e suportar refresh concorrente")
    void mvOrgHarvestSummary_agregaGrãoEExcluiCanceladosERefreshaConcorrentemente() {
        UUID orgId = insertOrganization("Org Teste MV");
        UUID commId = insertCommunity(orgId, "Comunidade MV");
        UUID farmerId = insertFarmer(commId, "Produtor MV", "55500055500", true, null);
        UUID harvestId = insertHarvest("Safra MV 2026");
        UUID cropId = insertCrop("Soja");

        // Plano ativo: 10 ha * 1000 kg/ha = 10000 kg target
        UUID activePlanId = insertProductionPlan(farmerId, harvestId, cropId, new BigDecimal("10.0"), new BigDecimal("1000.0"), "IN_PROGRESS", null);
        insertExecution(activePlanId, new BigDecimal("4000.0"), Date.valueOf(LocalDate.of(2026, 5, 1)), "VALIDATED", null);

        // Plano cancelado: 5 ha * 1000 kg/ha = 5000 kg target (não deve entrar na MV)
        UUID canceledPlanId = insertProductionPlan(farmerId, harvestId, cropId, new BigDecimal("5.0"), new BigDecimal("1000.0"), "CANCELED", null);
        insertExecution(canceledPlanId, new BigDecimal("2000.0"), Date.valueOf(LocalDate.of(2026, 5, 2)), "VALIDATED", null);

        // Executa REFRESH MATERIALIZED VIEW CONCURRENTLY
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_org_harvest_summary");

        Map<String, Object> summary = jdbcTemplate.queryForMap(
                """
                SELECT plan_count, farmer_count, planted_area_ha, target_kg, realized_kg, completion_pct
                FROM mv_org_harvest_summary
                WHERE organization_id = ? AND community_id = ? AND crop_id = ? AND harvest_id = ?
                """,
                orgId, commId, cropId, harvestId
        );

        assertThat(((Number) summary.get("plan_count")).intValue()).isEqualTo(1);
        assertThat(((Number) summary.get("farmer_count")).intValue()).isEqualTo(1);
        assertThat(new BigDecimal(summary.get("planted_area_ha").toString())).isEqualByComparingTo("10.00");
        assertThat(new BigDecimal(summary.get("target_kg").toString())).isEqualByComparingTo("10000.00");
        assertThat(new BigDecimal(summary.get("realized_kg").toString())).isEqualByComparingTo("4000.00");
        assertThat(new BigDecimal(summary.get("completion_pct").toString())).isEqualByComparingTo("40.00");
    }

    private UUID insertOrganization(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO organizations (id, name, tax_id, type, created_at) VALUES (?, ?, ?, 'COOPERATIVE', CURRENT_TIMESTAMP)",
                id, name, "TAX-" + UUID.randomUUID()
        );
        return id;
    }

    private UUID insertCommunity(UUID orgId, String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO communities (id, name, organization_id, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                id, name, orgId
        );
        return id;
    }

    private UUID insertFarmer(UUID commId, String fullName, String cpf, boolean isCompliant, java.sql.Timestamp deletedAt) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO farmers (id, community_id, full_name, cpf, registration_source, is_compliant, created_at, deleted_at)
                VALUES (?, ?, ?, ?, 'MANAGER', ?, CURRENT_TIMESTAMP, ?)
                """,
                id, commId, fullName, cpf, isCompliant, deletedAt
        );
        return id;
    }

    private UUID insertHarvest(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO harvests (id, name, start_date, end_date) VALUES (?, ?, '2026-01-01', '2026-12-31')",
                id, name
        );
        return id;
    }

    private UUID insertCrop(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO crop (id, name) VALUES (?, ?)",
                id, name
        );
        return id;
    }

    private UUID insertProductionPlan(UUID farmerId, UUID harvestId, UUID cropId, BigDecimal area, BigDecimal productivity, String status, java.sql.Timestamp deletedAt) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO production_plans (id, farmer_id, harvest_id, crop_id, planted_area, expected_productivity, status, created_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                """,
                id, farmerId, harvestId, cropId, area, productivity, status, deletedAt
        );
        return id;
    }

    private UUID insertExecution(UUID planId, BigDecimal quantityKg, Date harvestedAt, String status, java.sql.Timestamp deletedAt) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO production_executions (id, production_plan_id, quantity, quantity_kg, harvested_at, status, created_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)
                """,
                id, planId, quantityKg, quantityKg, harvestedAt, status, deletedAt
        );
        return id;
    }
}
