package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.TechnicalAssistance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TechnicalAssistanceRepository {
    TechnicalAssistance save(TechnicalAssistance assistance);
    Optional<TechnicalAssistance> findById(UUID id);
    List<TechnicalAssistance> findByTechnicianId(UUID technicianId);
    List<TechnicalAssistance> findByProducerId(UUID producerId);
    Optional<TechnicalAssistance> findActiveByTechnicianAndProducer(UUID technicianId, UUID producerId);
    void delete(UUID id);
}
