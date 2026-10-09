package com.ufal.smartagro.domain.model.enums;

/** Estratégia de frequência da colheita de uma cultura. */
public enum HarvestType {
    SINGLE("Colheita única"),
    CONTINUOUS("Colheita contínua"),
    STAGGERED("Colheita escalonada");

    private final String description;

    HarvestType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
