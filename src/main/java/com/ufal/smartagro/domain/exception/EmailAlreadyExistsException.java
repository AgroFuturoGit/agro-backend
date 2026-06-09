package com.ufal.smartagro.domain.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("O email informado já está em uso.");
    }

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
