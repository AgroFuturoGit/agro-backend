package com.ufal.smartagro.adapters.in.web.exception.dto;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String message,
        String path,
        LocalDateTime timestamp
) {
}
