package com.ufal.smartagro.adapters.in.web.dto.harvest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HarvestResponseDTO(
        UUID id,
        String label,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
