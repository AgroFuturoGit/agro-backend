package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/** Campos que podem ser alterados em um plano, incluindo estado, janela de colheita e localização. */
public record ProductionPlanUpdateDTO(
        @DecimalMin(value = "0.01", message = "A área de plantio deve ser maior que zero.")
        BigDecimal plantedArea,

        com.ufal.smartagro.domain.model.enums.PlanStatus status,
        LocalDate expectedHarvestStart,
        LocalDate expectedHarvestEnd,
        BigDecimal expectedProductivity,
        String locationDescription,
        BigDecimal latitude,
        BigDecimal longitude,

        LocalDate plannedPlantingDate,

        Map<String, Object> plannedCalendar,

        /**
         * Versão do plano que o cliente tinha em mãos ao editar. Quando enviada,
         * o servidor recusa a escrita com 409 caso já tenha avançado além dela.
         * Omitir mantém o comportamento de sobrescrita direta.
         */
        LocalDateTime baseUpdatedAt
) {
    /** Construtor legado mantido para compatibilidade; não grava mais expectedYield. */
    public ProductionPlanUpdateDTO(BigDecimal plantedArea, BigDecimal ignoredLegacyYield, LocalDate plannedPlantingDate,
                                   Map<String, Object> plannedCalendar, LocalDateTime baseUpdatedAt) {
        this(plantedArea, null, null, null, null, null, null, null, plannedPlantingDate, plannedCalendar, baseUpdatedAt);
    }
}
