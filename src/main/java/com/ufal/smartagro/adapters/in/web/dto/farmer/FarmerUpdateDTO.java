package com.ufal.smartagro.adapters.in.web.dto.farmer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FarmerUpdateDTO(
        @Size(max = 150, message = "O nome completo deve ter no máximo 150 caracteres")
        String fullName,

        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dateOfBirth,

        @Schema(description = "Nome da mãe do agricultor (opcional)", nullable = true)
        @Size(max = 150, message = "O nome da mãe deve ter no máximo 150 caracteres")
        String motherName,

        @Schema(description = "Naturalidade / local de origem do agricultor (ex: município/UF de nascimento ou procedência)", example = "Arapiraca - AL", nullable = true)
        @Size(max = 100, message = "A origem deve ter no máximo 100 caracteres")
        String origin,

        @Size(max = 100, message = "O nível de escolaridade deve ter no máximo 100 caracteres")
        String educationLevel,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String phone,

        @Size(max = 100, message = "O apelido/nome local deve ter no máximo 100 caracteres")
        String localName,

        @Size(max = 100, message = "A rua deve ter no máximo 100 caracteres")
        String street,

        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
        String city,

        @Size(max = 50, message = "O estado deve ter no máximo 50 caracteres")
        String state,

        @Size(max = 10, message = "O código IBGE deve ter no máximo 10 caracteres")
        String ibgeCode,

        @Schema(description = "Latitude da propriedade (opcional)", nullable = true)
        BigDecimal latitude,

        @Schema(description = "Longitude da propriedade (opcional)", nullable = true)
        BigDecimal longitude,

        @Schema(description = "Situação de conformidade do agricultor (alterável apenas por administradores, gerentes ou técnicos autorizados)")
        Boolean isCompliant
) {
    public FarmerUpdateDTO(String aliasName) {
        this(null, null, null, null, null, null, aliasName, null, null, null, null, null, null, null);
    }

    public String aliasName() {
        return localName;
    }
}
