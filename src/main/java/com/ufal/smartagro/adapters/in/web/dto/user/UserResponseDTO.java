package com.ufal.smartagro.adapters.in.web.dto.user;

import com.ufal.smartagro.domain.model.enums.Role;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String fullName,
        String email,
        String cpf,
        Role role,
        LocalDate dateOfBirth
) {
}
