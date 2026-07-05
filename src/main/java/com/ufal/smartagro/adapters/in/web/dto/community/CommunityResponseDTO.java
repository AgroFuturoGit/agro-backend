package com.ufal.smartagro.adapters.in.web.dto.community;

import java.time.LocalDateTime;
import java.util.UUID;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationResponseDTO;

public record CommunityResponseDTO(
    UUID id,
    String name,
    OrganizationResponseDTO organization,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
