package com.ufal.smartagro.adapters.in.web.dto.auth;

import com.ufal.smartagro.domain.model.enums.Role;

import java.util.UUID;

public record MyInfoResponseDTO(
    UUID id,
    String fullName,
    String email,
    Role role
) {}
