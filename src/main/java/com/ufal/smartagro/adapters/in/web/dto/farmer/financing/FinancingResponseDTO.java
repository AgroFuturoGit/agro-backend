package com.ufal.smartagro.adapters.in.web.dto.farmer.financing;

import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancingResponseDTO(
        @Schema(description = "Identificador único do financiamento", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Identificador do agricultor vinculado", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22")
        UUID farmerId,

        @Schema(description = "Descrição do financiamento", example = "PRONAF Custeio 2026")
        String description,

        @Schema(description = "Status do financiamento", example = "APPROVED")
        FinancingStatus status,

        @Schema(description = "Valor financiado em reais (R$)", example = "25000.00")
        BigDecimal amount,

        @Schema(description = "Data de cadastro")
        LocalDateTime createdAt,

        @Schema(description = "Data da última atualização")
        LocalDateTime updatedAt
) {
}
