package com.ufal.smartagro.domain.exception;

public class HarvestNotFoundException extends RuntimeException {
    public HarvestNotFoundException() {
        super("Safra não encontrada.");
    }

    public HarvestNotFoundException(String message) {
        super(message);
    }
}
