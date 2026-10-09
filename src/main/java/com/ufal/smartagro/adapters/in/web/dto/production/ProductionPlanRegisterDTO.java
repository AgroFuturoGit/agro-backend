package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** Dados recebidos para planejar uma cultura em uma safra e registrar sua área e localização. */
public record ProductionPlanRegisterDTO(
        @NotNull(message = "A safra é obrigatória.")
        UUID harvestId,

        @NotNull(message = "O cultivo é obrigatório.")
        UUID cropId,

        @NotNull(message = "A área de plantio é obrigatória.")
        @DecimalMin(value = "0.01", message = "A área de plantio deve ser maior que zero.")
        BigDecimal plantedArea,

        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd,
        String locationDescription,
        java.math.BigDecimal latitude,
        java.math.BigDecimal longitude,

        LocalDate plannedPlantingDate,

        Map<String, Object> plannedCalendar
) {
    /** Mantém compatibilidade com clientes Java antigos; o rendimento legado não substitui a produtividade da cultura. */
    public ProductionPlanRegisterDTO(UUID harvestId, UUID cropId, BigDecimal plantedArea, BigDecimal ignoredLegacyYield,
                                     LocalDate plannedPlantingDate, Map<String, Object> plannedCalendar) {
        this(harvestId, cropId, plantedArea, null, null, null, null, null, plannedPlantingDate, plannedCalendar);
    }
}
