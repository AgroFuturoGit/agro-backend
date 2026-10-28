package com.ufal.smartagro.adapters.in.web.dto.farmer.financing;

import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FinancingRegisterDTO(
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        @Schema(description = "Descrição do financiamento", example = "PRONAF Custeio 2026")
        String description,

        @NotNull(message = "O status é obrigatório")
        @Schema(description = "Status atual do financiamento", example = "APPROVED")
        FinancingStatus status,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "O valor deve ter no máximo 10 dígitos inteiros e 2 decimais")
        @Schema(description = "Valor financiado em reais (R$)", example = "25000.00")
        BigDecimal amount
) {
}
