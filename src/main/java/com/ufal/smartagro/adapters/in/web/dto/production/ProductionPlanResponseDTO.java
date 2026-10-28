package com.ufal.smartagro.adapters.in.web.dto.production;

import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/** Resposta do plano com produtividade snapshot; a meta é derivada da área vezes essa produtividade. */
public record ProductionPlanResponseDTO(
        UUID id,
        @Schema(description = "Identificador de idempotência originado pelo cliente offline", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID clientId,
        FarmerResponseDTO farmer,
        HarvestResponseDTO harvest,
        CropResponseDTO crop,
        @Schema(description = "Área plantada em hectares (ha)", example = "2.5")
        BigDecimal plantedArea,
        com.ufal.smartagro.domain.model.enums.PlanStatus status,
        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd,
        @Schema(description = "Produtividade esperada congelada na criação do plano em kg/ha", example = "12000.00")
        BigDecimal expectedProductivity,
        String locationDescription,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDate plannedPlantingDate,
        Map<String, Object> plannedCalendar,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Construtor de compatibilidade para chamadas sem clientId. */
    public ProductionPlanResponseDTO(
            UUID id, FarmerResponseDTO farmer, HarvestResponseDTO harvest, CropResponseDTO crop,
            BigDecimal plantedArea, com.ufal.smartagro.domain.model.enums.PlanStatus status,
            LocalDate expectedHarvestStart, LocalDate expectedHarvestEnd, BigDecimal expectedProductivity,
            String locationDescription, BigDecimal latitude, BigDecimal longitude,
            LocalDate plannedPlantingDate, Map<String, Object> plannedCalendar,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this(id, null, farmer, harvest, crop, plantedArea, status, expectedHarvestStart, expectedHarvestEnd,
                expectedProductivity, locationDescription, latitude, longitude, plannedPlantingDate,
                plannedCalendar, createdAt, updatedAt);
    }

    /** Valor derivado mantido para compatibilidade: não corresponde a uma coluna persistida. */
    public BigDecimal expectedYield() { return plantedArea.multiply(expectedProductivity); }
}
