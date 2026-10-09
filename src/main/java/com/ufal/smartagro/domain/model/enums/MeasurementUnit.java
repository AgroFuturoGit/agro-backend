package com.ufal.smartagro.domain.model.enums;

public enum MeasurementUnit {
    KG("Quilograma"),
    TON("Tonelada"),
    SACK("Saca"),
    BOX("Caixa");

    private final String description;

    MeasurementUnit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
