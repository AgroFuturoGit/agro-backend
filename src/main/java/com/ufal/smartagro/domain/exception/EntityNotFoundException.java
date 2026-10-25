package com.ufal.smartagro.domain.exception;

public class EntityNotFoundException extends IllegalArgumentException {
    public EntityNotFoundException() {
        super("Entidade não encontrada.");
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
