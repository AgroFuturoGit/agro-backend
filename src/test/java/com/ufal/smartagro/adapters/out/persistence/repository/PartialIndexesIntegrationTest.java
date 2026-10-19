package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PartialIndexesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final List<String> EXPECTED_PARTIAL_INDEXES = List.of(
            "idx_farmers_community_active",
            "idx_communities_org_active",
            "idx_managers_org_active",
            "idx_plans_farmer_active",
            "idx_plans_harvest_active",
            "idx_plans_crop_active",
            "idx_executions_plan_harvested_active",
            "idx_executions_plan_validated_active",
            "idx_ta_technician_community_active"
    );

    @Test
    @DisplayName("Todos os índices parciais devem existir com o predicado de soft delete")
    void partialIndexes_devemExistirComPredicadoSoftDelete() {
        for (String indexName : EXPECTED_PARTIAL_INDEXES) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' AND indexname = ?",
                    indexName
            );

            assertThat(rows)
                    .as("Índice parcial '%s' deve existir no schema public", indexName)
                    .hasSize(1);

            String indexDef = (String) rows.getFirst().get("indexdef");
            assertThat(indexDef.toUpperCase())
                    .as("Índice '%s' deve conter cláusula de soft delete", indexName)
                    .contains("DELETED_AT IS NULL");
        }
    }

    @Test
    @DisplayName("Índice de execuções validadas deve conter INCLUDE (quantity_kg) e status VALIDATED")
    void idxExecutionsPlanValidatedActive_deveConterIncludeEStatus() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT indexname, indexdef FROM pg_indexes WHERE schemaname = 'public' AND indexname = ?",
                "idx_executions_plan_validated_active"
        );

        assertThat(rows).hasSize(1);

        String indexDef = (String) rows.getFirst().get("indexdef");
        assertThat(indexDef.toUpperCase())
                .contains("INCLUDE (QUANTITY_KG)")
                .contains("VALIDATED");
    }
}
