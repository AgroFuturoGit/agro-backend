package com.ufal.smartagro.adapters.in.web.dto.role;

import com.ufal.smartagro.domain.model.enums.Role;
import jakarta.validation.constraints.NotNull;

public record AssignRoleDTO(
    @NotNull(message = "A role é obrigatória")
    Role role
) {}
