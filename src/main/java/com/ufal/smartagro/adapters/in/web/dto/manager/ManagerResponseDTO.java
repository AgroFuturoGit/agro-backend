package com.ufal.smartagro.adapters.in.web.dto.manager;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationResponseDTO;
import java.time.LocalDateTime;
import java.util.UUID;

public record ManagerResponseDTO(
    UUID id,
    UserResponseDTO user,
    OrganizationResponseDTO organization,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
