package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.FieldOccurrence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldOccurrenceRepository {
    FieldOccurrence save(FieldOccurrence fieldOccurrence);
    Optional<FieldOccurrence> findById(UUID id);
    List<FieldOccurrence> findAllByExecutionId(UUID executionId);
    Optional<FieldOccurrence> findByExecutionIdAndClientId(UUID executionId, UUID clientId);
    void delete(UUID id);
}
