package com.ufal.smartagro.adapters.in.web.dto.analytics;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Resumo dos dados cadastrais da safra para o dashboard.
 */
public record HarvestSummaryInfoDTO(
        UUID id,
        String label,
        LocalDate startDate,
        LocalDate endDate
) {
}
