package com.ufal.smartagro.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class ProductionPlanTest {
    @Test void deveCalcularMetaComoAreaMultiplicadaPelaProdutividade() {
        ProductionPlan plan = plano("2.5", "1200");
        assertThat(plan.getExpectedYield()).isEqualByComparingTo("3000");
    }
    @Test void deveRetornarMetaZeroQuandoAreaOuProdutividadeEstiverAusente() {
        assertThat(plano(null, "1200").getExpectedYield()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(plano("2.5", null).getExpectedYield()).isEqualByComparingTo(BigDecimal.ZERO);
    }
    @Test void deveConverterMetaLegadaEmProdutividadePorArea() {
        ProductionPlan legacy = new ProductionPlan(UUID.randomUUID(), null, null, null,
                new BigDecimal("3"), new BigDecimal("10"), null, null, null, null, null);
        assertThat(legacy.getExpectedProductivity()).isEqualByComparingTo("3.3333");
        assertThat(legacy.getExpectedYield()).isEqualByComparingTo("9.9999");
    }
    @Test void devePreservarSnapshotMesmoSeCulturaMudar() {
        Crop crop = new Crop(UUID.randomUUID(), "Milho", "A", false, 100, new BigDecimal("1200"), null, null, null);
        ProductionPlan plan = new ProductionPlan(UUID.randomUUID(), null, null, crop, new BigDecimal("2"),
                com.ufal.smartagro.domain.model.enums.PlanStatus.PLANNED, null, null, new BigDecimal("1200"),
                null, null, null, null, null, null, null, null);
        try {
            var productivity = Crop.class.getDeclaredField("expectedProductivity");
            productivity.setAccessible(true);
            productivity.set(crop, new BigDecimal("2000"));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        assertThat(plan.getExpectedProductivity()).isEqualByComparingTo("1200");
    }
    private ProductionPlan plano(String area, String productivity) {
        return new ProductionPlan(UUID.randomUUID(), null, null, null, area == null ? null : new BigDecimal(area),
                com.ufal.smartagro.domain.model.enums.PlanStatus.PLANNED, null, null,
                productivity == null ? null : new BigDecimal(productivity), null, null, null, null, null, null, null, null);
    }
}
