package com.ufal.smartagro.adapters.in.web.dto.farmer.financing;

import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record FinancingUpdateDTO(
        @Schema(description = "Descrição do financiamento", example = "PRONAF Custeio Safra 2026/2027")
        String description,

        @Schema(description = "Status atual do financiamento", example = "APPROVED")
        FinancingStatus status,

        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Schema(description = "Valor financiado em reais (R$)", example = "30000.00")
        BigDecimal amount
) {
}
