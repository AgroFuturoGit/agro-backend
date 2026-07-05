package com.ufal.smartagro.domain.exception;

public class CropAlreadyExistsException extends RuntimeException {
    public CropAlreadyExistsException() {
        super("A crop with the same name and variety already exists.");
    }

    public CropAlreadyExistsException(String message) {
        super(message);
    }
}
