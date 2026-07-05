package com.ufal.smartagro.domain.exception;

public class CropNotFoundException extends RuntimeException {
    public CropNotFoundException() {
        super("Crop not found.");
    }

    public CropNotFoundException(String message) {
        super(message);
    }
}
