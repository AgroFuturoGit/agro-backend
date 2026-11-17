package com.ufal.smartagro.adapters.in.web.dto.technicalassistance;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record TechnicalAssistanceResponseDTO(
        UUID id,
        UUID technicianId,
        UUID communityId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        CommunityResponseDTO community,
        Long activeFarmersCount
) {
    public TechnicalAssistanceResponseDTO(
            UUID id,
            UUID technicianId,
            UUID communityId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        this(id, technicianId, communityId, startDate, endDate, null, null);
    }
}
