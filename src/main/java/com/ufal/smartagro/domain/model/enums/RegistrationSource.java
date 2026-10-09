package com.ufal.smartagro.domain.model.enums;

public enum RegistrationSource {
    SELF("Autocadastro"),
    TECHNICIAN("Técnico"),
    MANAGER("Gestor");

    private final String description;

    RegistrationSource(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
