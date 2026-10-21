package com.ufal.smartagro.adapters.in.web.dto.production;

import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

/** Resposta do plano com produtividade snapshot; a meta é derivada da área vezes essa produtividade. */
public record ProductionPlanResponseDTO(
        UUID id,
        FarmerResponseDTO farmer,
        HarvestResponseDTO harvest,
        CropResponseDTO crop,
        @Schema(description = "Área plantada em hectares (ha).") BigDecimal plantedArea,
        com.ufal.smartagro.domain.model.enums.PlanStatus status,
        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd,
        @Schema(description = "Produtividade esperada em quilogramas por hectare (kg/ha). A meta derivada é plantedArea × expectedProductivity, em kg, comparável à soma de quantityKg.") BigDecimal expectedProductivity,
        String locationDescription,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDate plannedPlantingDate,
        Map<String, Object> plannedCalendar,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Valor derivado mantido para compatibilidade: não corresponde a uma coluna persistida. */
    @Schema(description = "Meta de produção derivada em quilogramas (kg): área em ha × produtividade em kg/ha.")
    public BigDecimal expectedYield() { return plantedArea.multiply(expectedProductivity); }
}
