package com.ufal.smartagro.adapters.in.web.dto.production;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductionDashboardDTO(
        long farmerCount,
        long planCount,
        BigDecimal expectedYield,
        BigDecimal actualYield,
        BigDecimal difference,
        BigDecimal percentageRealized,
        List<CommunityProductionDTO> communities
) {
    public record CommunityProductionDTO(
            UUID communityId,
            String communityName,
            long farmerCount,
            long planCount,
            BigDecimal expectedYield,
            BigDecimal actualYield,
            BigDecimal difference,
            BigDecimal percentageRealized
    ) {}
}
