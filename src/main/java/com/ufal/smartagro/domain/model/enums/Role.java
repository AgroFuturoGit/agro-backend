package com.ufal.smartagro.domain.model.enums;

public enum Role {
    ADMIN("Administrador do sistema"),
    MANAGER("Gestor de cooperativa/associação"),
    TECHNICIAN("Técnico agrícola"),
    PRODUCER("Agricultor/Produtor rural");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
    