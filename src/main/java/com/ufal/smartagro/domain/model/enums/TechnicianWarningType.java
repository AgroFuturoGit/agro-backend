package com.ufal.smartagro.domain.model.enums;

/**
 * Tipos de alertas operacionais e de risco identificados pelo motor de monitoramento do técnico.
 */
public enum TechnicianWarningType {
    INACTIVE_EXECUTIONS("Apontamentos atrasados"),
    LOW_YIELD_RISK("Risco de baixa produtividade"),
    HARVEST_OVERDUE("Janela de colheita vencida");

    private final String description;

    TechnicianWarningType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
