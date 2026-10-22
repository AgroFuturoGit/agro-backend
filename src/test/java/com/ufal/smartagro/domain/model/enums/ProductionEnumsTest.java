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
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TimelineGranularity.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> PlanProgressStatus.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TechnicianWarningType.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> WarningSeverity.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ManagementDashboardScope.valueOf("UNKNOWN")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void planProgressStatusDeveConterValoresEDescricoesEsperados() {
        assertThat(PlanProgressStatus.values()).containsExactly(
                PlanProgressStatus.GOAL_REACHED,
                PlanProgressStatus.ON_TRACK,
                PlanProgressStatus.BELOW_EXPECTED,
                PlanProgressStatus.DELAYED,
                PlanProgressStatus.NOT_STARTED
        );
        assertThat(PlanProgressStatus.GOAL_REACHED.getDescription()).isEqualTo("Meta Atingida");
        assertThat(PlanProgressStatus.ON_TRACK.getDescription()).isEqualTo("No Ritmo");
        assertThat(PlanProgressStatus.BELOW_EXPECTED.getDescription()).isEqualTo("Abaixo do Esperado");
        assertThat(PlanProgressStatus.DELAYED.getDescription()).isEqualTo("Atrasado");
        assertThat(PlanProgressStatus.NOT_STARTED.getDescription()).isEqualTo("Não Iniciado");
    }

    @Test void timelineGranularityDeveConterValoresEDescricoesEsperados() {
        assertThat(TimelineGranularity.values()).containsExactly(TimelineGranularity.WEEK, TimelineGranularity.MONTH);
        assertThat(TimelineGranularity.WEEK.getDescription()).isEqualTo("Semana");
        assertThat(TimelineGranularity.MONTH.getDescription()).isEqualTo("Mês");
    }

    @Test void timelineGranularityFromStringDeveFazerParseCaseInsensitiveERetornarPadrao() {
        assertThat(TimelineGranularity.fromString(null)).isEqualTo(TimelineGranularity.WEEK);
        assertThat(TimelineGranularity.fromString("")).isEqualTo(TimelineGranularity.WEEK);
        assertThat(TimelineGranularity.fromString("   ")).isEqualTo(TimelineGranularity.WEEK);
        assertThat(TimelineGranularity.fromString("week")).isEqualTo(TimelineGranularity.WEEK);
        assertThat(TimelineGranularity.fromString("WEEK")).isEqualTo(TimelineGranularity.WEEK);
        assertThat(TimelineGranularity.fromString("month")).isEqualTo(TimelineGranularity.MONTH);
        assertThat(TimelineGranularity.fromString("MONTH")).isEqualTo(TimelineGranularity.MONTH);
        assertThat(TimelineGranularity.fromString("Semana")).isEqualTo(TimelineGranularity.WEEK);
        assertThat(TimelineGranularity.fromString("Mês")).isEqualTo(TimelineGranularity.MONTH);
        assertThat(TimelineGranularity.fromString("mes")).isEqualTo(TimelineGranularity.MONTH);
    }

    @Test void timelineGranularityFromStringDeveRejeitarValorInvalido() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TimelineGranularity.fromString("YEAR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Granularidade inválida: 'YEAR'");
    }

    @Test void technicianWarningTypeDeveConterValoresEDescricoesEsperados() {
        assertThat(TechnicianWarningType.values()).containsExactly(
                TechnicianWarningType.INACTIVE_EXECUTIONS,
                TechnicianWarningType.LOW_YIELD_RISK,
                TechnicianWarningType.HARVEST_OVERDUE
        );
        assertThat(TechnicianWarningType.INACTIVE_EXECUTIONS.getDescription()).isEqualTo("Apontamentos atrasados");
        assertThat(TechnicianWarningType.LOW_YIELD_RISK.getDescription()).isEqualTo("Risco de baixa produtividade");
        assertThat(TechnicianWarningType.HARVEST_OVERDUE.getDescription()).isEqualTo("Janela de colheita vencida");
    }

    @Test void warningSeverityDeveConterValoresEDescricoesEsperados() {
        assertThat(WarningSeverity.values()).containsExactly(
                WarningSeverity.MEDIUM,
                WarningSeverity.HIGH
        );
        assertThat(WarningSeverity.MEDIUM.getDescription()).isEqualTo("Média");
        assertThat(WarningSeverity.HIGH.getDescription()).isEqualTo("Alta");
    }

    @Test void managementDashboardScopeDeveConterValoresEDescricoesEsperados() {
        assertThat(ManagementDashboardScope.values()).containsExactly(
                ManagementDashboardScope.ORGANIZATION,
                ManagementDashboardScope.GLOBAL
        );
        assertThat(ManagementDashboardScope.ORGANIZATION.getDescription()).isEqualTo("Organização");
        assertThat(ManagementDashboardScope.GLOBAL.getDescription()).isEqualTo("Global");
    }
}

