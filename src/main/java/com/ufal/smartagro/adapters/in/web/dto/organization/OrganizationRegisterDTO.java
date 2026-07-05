package com.ufal.smartagro.adapters.in.web.dto.organization;

import com.ufal.smartagro.domain.model.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrganizationRegisterDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name,
    @NotBlank(message = "O CNPJ/taxId é obrigatório")
    String taxId,
    @NotNull(message = "O tipo é obrigatório")
    OrganizationType type
) {}
