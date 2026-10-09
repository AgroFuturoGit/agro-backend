package com.ufal.smartagro.adapters.in.web.dto.user;

import com.ufal.smartagro.domain.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUserUpdateDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        String fullName,

        @NotNull(message = "O papel é obrigatório")
        Role role
) {
}
