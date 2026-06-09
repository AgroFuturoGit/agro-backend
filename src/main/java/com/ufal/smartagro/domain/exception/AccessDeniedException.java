package com.ufal.smartagro.domain.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {
        super("Acesso negado. Somente administradores podem realizar esta ação.");
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
