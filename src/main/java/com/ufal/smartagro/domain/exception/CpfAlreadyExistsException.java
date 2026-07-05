package com.ufal.smartagro.domain.exception;

public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException() {
        super("O CPF informado já está em uso.");
    }

    public CpfAlreadyExistsException(String message) {
        super(message);
    }
}
