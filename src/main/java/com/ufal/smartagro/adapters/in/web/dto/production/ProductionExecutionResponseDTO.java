package com.ufal.smartagro.adapters.in.web.dto.production;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Representação do apontamento devolvida pela API, com dados de colheita e validação. */
public record ProductionExecutionResponseDTO(
        UUID id,
        @Schema(description = "Identificador de idempotência originado pelo cliente offline", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID clientId,
        UUID productionPlanId,
        BigDecimal quantity,
        BigDecimal quantityKg,
        LocalDate harvestedAt,
        com.ufal.smartagro.domain.model.enums.ExecutionStatus status,
        UUID validatedBy,
        LocalDateTime validatedAt,
        String notes,
        String rejectionReason,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal locationAccuracy,
        LocalDateTime locationRecordedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Construtor de compatibilidade para chamadas sem clientId. */
    public ProductionExecutionResponseDTO(
            UUID id, UUID productionPlanId, BigDecimal quantity, BigDecimal quantityKg, LocalDate harvestedAt,
            com.ufal.smartagro.domain.model.enums.ExecutionStatus status, UUID validatedBy, LocalDateTime validatedAt,
            String notes, String rejectionReason, BigDecimal latitude, BigDecimal longitude,
            BigDecimal locationAccuracy, LocalDateTime locationRecordedAt, LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, null, productionPlanId, quantity, quantityKg, harvestedAt, status, validatedBy, validatedAt,
                notes, rejectionReason, latitude, longitude, locationAccuracy, locationRecordedAt, createdAt, updatedAt);
    }

    /** Construtor legado de compatibilidade para chamadas sem rejectionReason e sem clientId. */
    public ProductionExecutionResponseDTO(
            UUID id, UUID productionPlanId, BigDecimal quantity, BigDecimal quantityKg, LocalDate harvestedAt,
            com.ufal.smartagro.domain.model.enums.ExecutionStatus status, UUID validatedBy, LocalDateTime validatedAt,
            String notes, BigDecimal latitude, BigDecimal longitude, BigDecimal locationAccuracy,
            LocalDateTime locationRecordedAt, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this(id, null, productionPlanId, quantity, quantityKg, harvestedAt, status, validatedBy, validatedAt, notes, null,
                latitude, longitude, locationAccuracy, locationRecordedAt, createdAt, updatedAt);
    }
}
