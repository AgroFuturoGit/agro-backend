package com.ufal.smartagro.adapters.in.web.dto.user;

import com.ufal.smartagro.domain.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        @Size(min = 2, max = 150, message = "O nome completo deve ter entre 2 e 150 caracteres")
        String fullName,

        @NotNull(message = "O papel é obrigatório")
        Role role
) {
}
