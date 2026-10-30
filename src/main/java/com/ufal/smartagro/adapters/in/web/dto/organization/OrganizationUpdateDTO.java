package com.ufal.smartagro.adapters.in.web.dto.organization;

import com.ufal.smartagro.domain.model.enums.OrganizationType;
import jakarta.validation.constraints.Size;

public record OrganizationUpdateDTO(
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String name,

        OrganizationType type
) {
}
