package com.ufal.smartagro.domain.model.enums;

public enum ProfessionalRegistrationType {
    CREA("Conselho Regional de Engenharia e Agronomia"),
    CFT("Conselho Federal dos Técnicos Industriais"),
    CFTA("Conselho Federal dos Técnicos Agrícolas");

    private final String description;

    ProfessionalRegistrationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
