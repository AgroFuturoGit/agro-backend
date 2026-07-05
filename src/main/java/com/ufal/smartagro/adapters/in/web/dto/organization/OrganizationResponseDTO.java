package com.ufal.smartagro.adapters.in.web.dto.organization;

import com.ufal.smartagro.domain.model.enums.OrganizationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizationResponseDTO(
    UUID id,
    String name,
    String taxId,
    OrganizationType type,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
