package com.ufal.smartagro.adapters.in.web.exception.dto;

import java.time.LocalDateTime;

/**
 * Corpo do 409 de alteração concorrente. Repete o formato do {@link ApiError} e
 * acrescenta {@code current}, a versão que está no servidor, para que o cliente
 * reconcilie o dado local sem precisar de uma segunda requisição.
 */
public record ConflictApiError(
        int status,
        String message,
        String path,
        LocalDateTime timestamp,
        Object current
) {
}
