package com.ufal.smartagro.domain.exception;

public class CropNotFoundException extends RuntimeException {
    public CropNotFoundException() {
        super("Cultura não encontrada.");
    }

    public CropNotFoundException(String message) {
        super(message);
    }
}
