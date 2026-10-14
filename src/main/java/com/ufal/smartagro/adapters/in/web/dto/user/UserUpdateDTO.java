package com.ufal.smartagro.adapters.in.web.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        String fullName
) {
}
