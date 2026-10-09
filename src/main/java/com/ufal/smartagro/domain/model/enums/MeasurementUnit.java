package com.ufal.smartagro.domain.model.enums;

/** Unidades aceitas para informar a quantidade colhida. */
public enum MeasurementUnit {
    KG("Quilograma"),
    TON("Tonelada"),
    BAG("Saca"),
    BOX("Caixa");

    private final String description;

    MeasurementUnit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
