package com.ufal.smartagro.domain.model.enums;

/**
 * Nível de severidade para alertas do painel operacional do técnico.
 */
public enum WarningSeverity {
    MEDIUM("Média"),
    HIGH("Alta");

    private final String description;

    WarningSeverity(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
