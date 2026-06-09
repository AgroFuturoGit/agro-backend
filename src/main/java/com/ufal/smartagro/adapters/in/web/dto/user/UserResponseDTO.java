package com.ufal.smartagro.adapters.in.web.dto.user;

import com.ufal.smartagro.domain.model.enums.Role;

import java.time.LocalDate;

public record UserResponseDTO(
        Long id,
        String fullName,
        String email,
        String cpf,
        Role role,
        LocalDate dateOfBirth
) {
}
