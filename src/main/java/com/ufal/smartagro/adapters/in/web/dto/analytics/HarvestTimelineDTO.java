package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;

import java.util.List;
import java.util.UUID;

/**
 * Resposta completa do endpoint de evolução temporal analítica da colheita.
 */
public record HarvestTimelineDTO(
        UUID planId,
        HarvestType harvestType,
        TimelineGranularity granularity,
        String unit,
        boolean hasExpectedWindow,
        List<TimelinePointDTO> points
) {
    public HarvestTimelineDTO(
            UUID planId,
            HarvestType harvestType,
            TimelineGranularity granularity,
            boolean hasExpectedWindow,
            List<TimelinePointDTO> points
    ) {
        this(planId, harvestType, granularity, "kg", hasExpectedWindow, points);
    }

    public HarvestTimelineDTO(
            UUID planId,
            HarvestType harvestType,
            TimelineGranularity granularity,
            String unit,
            List<TimelinePointDTO> points
    ) {
        this(planId, harvestType, granularity, unit, false, points);
    }

    public HarvestTimelineDTO(
            UUID planId,
            HarvestType harvestType,
            TimelineGranularity granularity,
            List<TimelinePointDTO> points
    ) {
        this(planId, harvestType, granularity, "kg", false, points);
    }
}
