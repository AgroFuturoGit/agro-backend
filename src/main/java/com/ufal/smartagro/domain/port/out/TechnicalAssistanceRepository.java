package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.TechnicalAssistance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TechnicalAssistanceRepository {
    TechnicalAssistance save(TechnicalAssistance assistance);
    Optional<TechnicalAssistance> findById(UUID id);
    List<TechnicalAssistance> findByTechnicianId(UUID technicianId);
    List<TechnicalAssistance> findByFarmerId(UUID farmerId);
    Optional<TechnicalAssistance> findActiveByTechnicianAndFarmer(UUID technicianId, UUID farmerId);
    void delete(UUID id);
}
