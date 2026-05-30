package com.ufal.smartagro.adapters.in.web.dto.auth;

import com.ufal.smartagro.domain.model.enums.Role;

public record MyInfoResponseDTO(
    Long id,
    String fullName,
    String email,
    Role role
) {}
