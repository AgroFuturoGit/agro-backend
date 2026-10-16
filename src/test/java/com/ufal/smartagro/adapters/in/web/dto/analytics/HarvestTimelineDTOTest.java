package com.ufal.smartagro.adapters.in.web.dto.analytics;

import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HarvestTimelineDTOTest {

    @Test
    void deveCriarTimelinePointDTOCorretamente() {
        LocalDate date = LocalDate.of(2026, 5, 1);
        BigDecimal expected = new BigDecimal("100.00");
        BigDecimal actual = new BigDecimal("80.00");
        BigDecimal pending = new BigDecimal("15.00");

        TimelinePointDTO point = new TimelinePointDTO(date, expected, actual, pending);

        assertThat(point.date()).isEqualTo(date);
        assertThat(point.expectedValue()).isEqualTo(expected);
        assertThat(point.actualValue()).isEqualTo(actual);
        assertThat(point.pendingValue()).isEqualTo(pending);
    }

    @Test
    void deveCriarHarvestTimelineDTOComUnidadePadraoKg() {
        UUID planId = UUID.randomUUID();
        TimelinePointDTO point = new TimelinePointDTO(
                LocalDate.of(2026, 5, 1),
                new BigDecimal("100.00"),
                new BigDecimal("80.00"),
                new BigDecimal("0.00")
        );

        HarvestTimelineDTO dto = new HarvestTimelineDTO(
                planId,
                HarvestType.CONTINUOUS,
                TimelineGranularity.WEEK,
                List.of(point)
        );

        assertThat(dto.planId()).isEqualTo(planId);
        assertThat(dto.harvestType()).isEqualTo(HarvestType.CONTINUOUS);
        assertThat(dto.granularity()).isEqualTo(TimelineGranularity.WEEK);
        assertThat(dto.unit()).isEqualTo("kg");
        assertThat(dto.points()).containsExactly(point);
    }

    @Test
    void deveCriarHarvestTimelineDTOComUnidadeCustomizada() {
        UUID planId = UUID.randomUUID();
        HarvestTimelineDTO dto = new HarvestTimelineDTO(
                planId,
                HarvestType.SINGLE,
                TimelineGranularity.MONTH,
                "saca",
                List.of()
        );

        assertThat(dto.unit()).isEqualTo("saca");
        assertThat(dto.points()).isEmpty();
    }
}
