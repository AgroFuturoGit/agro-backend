package com.ufal.smartagro.adapters.in.web.dto.harvest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HarvestUpdateDTO(
        @NotBlank(message = "O rótulo é obrigatório")
        @Size(max = 100, message = "O rótulo deve ter no máximo 100 caracteres")
        String label,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate startDate,

        @NotNull(message = "A data de término é obrigatória")
        LocalDate endDate,

        LocalDateTime baseUpdatedAt
) {
    public HarvestUpdateDTO(String label, LocalDate startDate, LocalDate endDate) {
        this(label, startDate, endDate, null);
    }
}
