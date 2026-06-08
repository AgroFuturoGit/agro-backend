package com.ufal.smartagro.adapters.in.web.dto.harvest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record HarvestRegisterDTO(
        @NotBlank(message = "O rótulo é obrigatório")
        String label,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate startDate,

        @NotNull(message = "A data de término é obrigatória")
        LocalDate endDate
) {
}
