package com.ufal.smartagro.domain.exception;

public class HarvestAlreadyExistsException extends RuntimeException {
    public HarvestAlreadyExistsException() {
        super("Uma safra com o mesmo rótulo ou período já existe.");
    }

    public HarvestAlreadyExistsException(String message) {
        super(message);
    }
}
