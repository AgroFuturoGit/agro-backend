package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record ProductionPlanUpdateDTO(
        @DecimalMin(value = "0.01", message = "A área de plantio deve ser maior que zero.")
        BigDecimal plantedArea,

        @DecimalMin(value = "0.01", message = "O rendimento esperado deve ser maior que zero.")
        BigDecimal expectedYield,

        LocalDate plannedPlantingDate,

        Map<String, Object> plannedCalendar,

        /**
         * Versão do plano que o cliente tinha em mãos ao editar. Quando enviada,
         * o servidor recusa a escrita com 409 caso já tenha avançado além dela.
         * Omitir mantém o comportamento de sobrescrita direta.
         */
        LocalDateTime baseUpdatedAt
) {
}
