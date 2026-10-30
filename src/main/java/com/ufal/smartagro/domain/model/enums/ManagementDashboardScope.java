package com.ufal.smartagro.domain.model.enums;

/**
 * Escopo de visualização do dashboard macro-gerencial.
 */
public enum ManagementDashboardScope {
    ORGANIZATION("Organização"),
    GLOBAL("Global");

    private final String description;

    ManagementDashboardScope(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
