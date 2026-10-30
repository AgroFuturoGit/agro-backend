package com.ufal.smartagro.adapters.in.web.dto.technician;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record TechnicianResponseDTO(
    UUID id,
    UserResponseDTO user,
    ProfessionalRegistrationType registrationType,
    String registrationNumber,
    String professionalId,
    String specialty,
    UserResponseDTO createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
