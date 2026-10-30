package com.ufal.smartagro.domain.model.enums;

/** Estados de revisão aplicáveis a um apontamento de colheita. */
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
