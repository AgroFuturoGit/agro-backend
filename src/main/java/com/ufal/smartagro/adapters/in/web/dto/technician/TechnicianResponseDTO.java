package com.ufal.smartagro.adapters.in.web.dto.technician;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record TechnicianResponseDTO(
    UUID id,
    UserResponseDTO user,
    String professionalId,
    String specialty,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
