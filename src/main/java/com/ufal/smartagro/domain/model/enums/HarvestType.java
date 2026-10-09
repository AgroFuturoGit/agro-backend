package com.ufal.smartagro.domain.model.enums;

public enum HarvestType {
    SINGLE("Colheita única"),
    CONTINUOUS("Colheita contínua");

    private final String description;

    HarvestType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
