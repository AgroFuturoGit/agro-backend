package com.ufal.smartagro.adapters.in.web.dto.organization;

import com.ufal.smartagro.domain.model.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrganizationRegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String name,

        @NotBlank(message = "O CNPJ/taxId é obrigatório")
        @Pattern(regexp = "(^\\d{14}$)|(^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$)", message = "O CNPJ/taxId deve ser válido")
        String taxId,

        @NotNull(message = "O tipo é obrigatório")
        OrganizationType type
) {
}
