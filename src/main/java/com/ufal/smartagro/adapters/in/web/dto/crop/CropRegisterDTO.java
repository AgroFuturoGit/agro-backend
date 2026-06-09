package com.ufal.smartagro.adapters.in.web.dto.crop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CropRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "A variedade é obrigatória")
        String variety,

        @NotNull(message = "O campo isPriority é obrigatório")
        Boolean isPriority
) {
}
