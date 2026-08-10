package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Technician;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TechnicianRepository {
    Technician save(Technician technician);
    Optional<Technician> findById(UUID id);
    Optional<Technician> findByUserId(UUID userId);
    List<Technician> findAll();
    List<Technician> findAllByOrganizationId(UUID organizationId);
    boolean existsByIdAndOrganizationId(UUID technicianId, UUID organizationId);
    void delete(UUID id);
}
