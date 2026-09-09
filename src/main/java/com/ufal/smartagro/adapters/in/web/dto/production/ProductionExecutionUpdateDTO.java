package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductionExecutionUpdateDTO(
        @DecimalMin(value = "0.01", message = "A quantidade produzida deve ser maior que zero.")
        BigDecimal actualYield,

        LocalDate harvestDate,

        /**
         * Versão do apontamento que o cliente tinha em mãos ao editar. Quando
         * enviada, o servidor recusa a escrita com 409 caso já tenha avançado
         * além dela. Omitir mantém o comportamento de sobrescrita direta.
         */
        LocalDateTime baseUpdatedAt
) {
}
