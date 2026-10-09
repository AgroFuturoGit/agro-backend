package com.ufal.smartagro.domain.model.enums;

/** Estados possíveis do ciclo de vida de um plano de produção. */
public enum PlanStatus {
    DRAFT("Rascunho"),
    PLANNED("Planejado"),
    IN_PROGRESS("Em andamento"),
    ACTIVE("Ativo"),
    FINISHED("Finalizado"),
    CANCELED("Cancelado");

    private final String description;

    PlanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
