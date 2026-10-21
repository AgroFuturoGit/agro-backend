package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FieldOccurrenceTest {

    @Test
    void deveCriarFieldOccurrenceCorretamente() {
        UUID id = UUID.randomUUID();
        ProductionExecution execution = ProductionExecution.builder().id(UUID.randomUUID()).build();
        LocalDateTime now = LocalDateTime.now();

        FieldOccurrence occurrence = FieldOccurrence.builder()
                .id(id)
                .execution(execution)
                .logType(OccurrenceType.PEST)
                .description("Incidência de lagarta do cartucho na área sul")
                .weatherObservation("Sol forte, 32°C")
                .evidenceUrl("https://storage.smartagro.test/evidence/123.jpg")
                .createdAt(now)
                .build();

        assertThat(occurrence.getId()).isEqualTo(id);
        assertThat(occurrence.getExecution()).isEqualTo(execution);
        assertThat(occurrence.getLogType()).isEqualTo(OccurrenceType.PEST);
        assertThat(occurrence.getDescription()).isEqualTo("Incidência de lagarta do cartucho na área sul");
        assertThat(occurrence.getWeatherObservation()).isEqualTo("Sol forte, 32°C");
        assertThat(occurrence.getEvidenceUrl()).isEqualTo("https://storage.smartagro.test/evidence/123.jpg");
        assertThat(occurrence.getCreatedAt()).isEqualTo(now);
    }
}
