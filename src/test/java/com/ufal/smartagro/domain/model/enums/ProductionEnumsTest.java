package com.ufal.smartagro.domain.model.enums;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ProductionEnumsTest {
    @Test void harvestTypeDeveConterValoresEDescricoesEsperados() {
        assertThat(HarvestType.values()).containsExactly(HarvestType.SINGLE, HarvestType.CONTINUOUS);
        assertThat(HarvestType.SINGLE.getDescription()).isEqualTo("Colheita única");
        assertThat(HarvestType.CONTINUOUS.getDescription()).isEqualTo("Colheita contínua");
    }
    @Test void measurementUnitDeveConterValoresEDescricoesEsperados() {
        assertThat(MeasurementUnit.values()).containsExactly(MeasurementUnit.KG, MeasurementUnit.TON, MeasurementUnit.SACK, MeasurementUnit.BOX);
        assertThat(MeasurementUnit.KG.getDescription()).isEqualTo("Quilograma");
        assertThat(MeasurementUnit.TON.getDescription()).isEqualTo("Tonelada");
        assertThat(MeasurementUnit.SACK.getDescription()).isEqualTo("Saca");
        assertThat(MeasurementUnit.BOX.getDescription()).isEqualTo("Caixa");
    }
    @Test void planStatusDeveConterValoresEDescricoesEsperados() {
        assertThat(PlanStatus.values()).containsExactly(PlanStatus.PLANNED, PlanStatus.ACTIVE, PlanStatus.FINISHED, PlanStatus.CANCELED);
        assertThat(PlanStatus.PLANNED.getDescription()).isEqualTo("Planejado");
        assertThat(PlanStatus.ACTIVE.getDescription()).isEqualTo("Ativo");
        assertThat(PlanStatus.FINISHED.getDescription()).isEqualTo("Finalizado");
        assertThat(PlanStatus.CANCELED.getDescription()).isEqualTo("Cancelado");
    }
    @Test void executionStatusDeveConterValoresEDescricoesEsperados() {
        assertThat(ExecutionStatus.values()).containsExactly(ExecutionStatus.PENDING, ExecutionStatus.VALIDATED, ExecutionStatus.REJECTED);
        assertThat(ExecutionStatus.PENDING.getDescription()).isEqualTo("Pendente de validação");
        assertThat(ExecutionStatus.VALIDATED.getDescription()).isEqualTo("Validado");
        assertThat(ExecutionStatus.REJECTED.getDescription()).isEqualTo("Rejeitado");
    }
    @Test void valueOfDeveRejeitarValorDesconhecido() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ExecutionStatus.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> PlanStatus.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> HarvestType.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> MeasurementUnit.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
    }
}
