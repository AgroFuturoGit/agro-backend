package com.ufal.smartagro.domain.model.enums;

public enum PlanStatus {
    PLANNED("Planejado"),
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
