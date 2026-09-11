package com.ufal.smartagro.adapters.in.web.dto.technicalassistance;

import java.time.LocalDateTime;
import java.util.UUID;

public record TechnicalAssistanceResponseDTO(
        UUID id,
        UUID technicianId,
        UUID farmerId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
