package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Ponto individual na linha do tempo da colheita com valores planejados, validados e pendentes.
 */
public record TimelinePointDTO(
        LocalDate date,
        BigDecimal expectedValue,
        BigDecimal actualValue,
        BigDecimal pendingValue
) {
}
