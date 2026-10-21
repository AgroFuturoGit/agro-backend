package com.ufal.smartagro.domain.model.enums;

public enum FinancingStatus {
    PENDING("Pendente"),
    IN_ANALYSIS("Em Análise"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado"),
    CANCELED("Cancelado"),
    PAID("Quitado");

    private final String description;

    FinancingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
