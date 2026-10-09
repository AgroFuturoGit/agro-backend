package com.ufal.smartagro.domain.model.enums;

public enum ExecutionStatus {
    PENDING("Pendente de validação"),
    VALIDATED("Validado"),
    REJECTED("Rejeitado");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
