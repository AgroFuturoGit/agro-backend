package com.ufal.smartagro.domain.exception;

public class CropAlreadyExistsException extends RuntimeException {
    public CropAlreadyExistsException() {
        super("Já existe uma cultura cadastrada com este nome e variedade.");
    }

    public CropAlreadyExistsException(String message) {
        super(message);
    }
}
