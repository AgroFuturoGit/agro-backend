package com.ufal.smartagro.adapters.in.web.dto.user;

import com.ufal.smartagro.domain.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AdminUserUpdateDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        String fullName,

        @NotNull(message = "A data de nascimento é obrigatória")
        LocalDate dateOfBirth,

        @NotNull(message = "O papel é obrigatório")
        Role role
) {
}
