package com.ufal.smartagro.adapters.in.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
