package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayBackfillIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID planId;

    @BeforeEach
    void setUp() {
        UUID orgId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO organizations (id, name, tax_id, type, created_at) VALUES (?, ?, ?, 'COOPERATIVE', CURRENT_TIMESTAMP)",
                orgId, "Org Backfill", "TAX-" + UUID.randomUUID()
        );

        UUID commId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO communities (id, name, organization_id, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                commId, "Comunidade Backfill", orgId
        );

        UUID farmerId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO farmers (id, community_id, full_name, cpf, registration_source, is_compliant, created_at)
                VALUES (?, ?, 'Produtor Backfill', ?, 'MANAGER', true, CURRENT_TIMESTAMP)
                """,
                farmerId, commId, "12345678901"
        );

        UUID harvestId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO harvests (id, label, start_date, end_date, created_at) VALUES (?, 'Safra Backfill', '2026-01-01', '2026-12-31', CURRENT_TIMESTAMP)",
                harvestId
        );

        UUID cropId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO crop (id, name, variety, is_priority, created_at) VALUES (?, 'Milho', 'Comum', true, CURRENT_TIMESTAMP)",
                cropId
        );

        planId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO production_plans (id, farmer_id, harvest_id, crop_id, planted_area, expected_productivity, status, created_at)
                VALUES (?, ?, ?, ?, 10.0, 100.0, 'IN_PROGRESS', CURRENT_TIMESTAMP)
                """,
                planId, farmerId, harvestId, cropId
        );
    }

    @Test
    @DisplayName("Backfill deve migrar motivo da rejeição concatenado em notes para rejection_reason")
    void backfill_migraMotivoCorretamente() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO production_executions (
                id, production_plan_id, quantity, quantity_kg, harvested_at, status, notes, rejection_reason, created_at
            ) VALUES (
                ?, ?, 10.0, 10.0, CURRENT_DATE, 'REJECTED', 'Observação de campo | [Motivo da rejeição]: Foto ilegível', NULL, CURRENT_TIMESTAMP
            )
            """, id, planId);

        jdbcTemplate.execute("""
            UPDATE production_executions
            SET rejection_reason = substring(notes FROM '\\[Motivo da rejeição\\]: (.*)$'),
                notes = NULLIF(btrim(regexp_replace(notes, '\\s*\\|?\\s*\\[Motivo da rejeição\\]: .*$', '')), '')
            WHERE id = '""" + id + "' AND status = 'REJECTED' AND notes LIKE '%[Motivo da rejeição]: %'");

        Map<String, Object> result = jdbcTemplate.queryForMap(
                "SELECT notes, rejection_reason FROM production_executions WHERE id = ?", id);

        assertThat(result.get("notes")).isEqualTo("Observação de campo");
        assertThat(result.get("rejection_reason")).isEqualTo("Foto ilegível");
    }

    @Test
    @DisplayName("Backfill deve limpar notes completamente quando continha apenas o motivo da rejeição")
    void backfill_limpaNotesQuandoSoTinhaMotivo() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO production_executions (
                id, production_plan_id, quantity, quantity_kg, harvested_at, status, notes, rejection_reason, created_at
            ) VALUES (
                ?, ?, 10.0, 10.0, CURRENT_DATE, 'REJECTED', '[Motivo da rejeição]: Produto estragado', NULL, CURRENT_TIMESTAMP
            )
            """, id, planId);

        jdbcTemplate.execute("""
            UPDATE production_executions
            SET rejection_reason = substring(notes FROM '\\[Motivo da rejeição\\]: (.*)$'),
                notes = NULLIF(btrim(regexp_replace(notes, '\\s*\\|?\\s*\\[Motivo da rejeição\\]: .*$', '')), '')
            WHERE id = '""" + id + "' AND status = 'REJECTED' AND notes LIKE '%[Motivo da rejeição]: %'");

        Map<String, Object> result = jdbcTemplate.queryForMap(
                "SELECT notes, rejection_reason FROM production_executions WHERE id = ?", id);

        assertThat(result.get("notes")).isNull();
        assertThat(result.get("rejection_reason")).isEqualTo("Produto estragado");
    }
}
