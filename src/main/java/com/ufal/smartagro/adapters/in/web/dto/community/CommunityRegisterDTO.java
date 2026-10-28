package com.ufal.smartagro.adapters.in.web.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityRegisterDTO(
        @NotBlank(message = "O nome da comunidade é obrigatório")
        @Size(max = 150, message = "O nome da comunidade deve ter no máximo 150 caracteres")
        String name
) {
}
