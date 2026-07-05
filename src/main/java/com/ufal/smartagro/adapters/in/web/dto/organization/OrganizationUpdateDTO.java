package com.ufal.smartagro.adapters.in.web.dto.organization;

import com.ufal.smartagro.domain.model.enums.OrganizationType;

public record OrganizationUpdateDTO(
    String name,
    OrganizationType type
) {}
