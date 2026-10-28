package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldOccurrence {
    private UUID id;
    /** Identificador de idempotência gerado no cliente offline. */
    private UUID clientId;
    private ProductionExecution execution;
    private OccurrenceType logType;
    private String description;
    private String weatherObservation;
    private String evidenceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
