package com.ufal.smartagro.domain.model.enums;

public enum OrganizationType {
    COOP("Cooperativa"),
    ASSOC("Associação");

    private final String description;

    OrganizationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
