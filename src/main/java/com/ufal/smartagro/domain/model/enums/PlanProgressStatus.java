package com.ufal.smartagro.domain.model.enums;

/**
 * Indicador visual e de saúde do progresso de um plano de produção.
 */
public enum PlanProgressStatus {
    GOAL_REACHED("Meta Atingida"),
    ON_TRACK("No Ritmo"),
    BELOW_EXPECTED("Abaixo do Esperado"),
    DELAYED("Atrasado"),
    NOT_STARTED("Não Iniciado");

    private final String description;

    PlanProgressStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
