package com.ufal.smartagro.domain.model.enums;

public enum OccurrenceType {
    CLIMATE("Evento Climático"),
    PEST("Praga"),
    DISEASE("Doença");

    private final String description;

    OccurrenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
