package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Manager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository {
    Manager save(Manager manager);

    Optional<Manager> findById(UUID id);

    Optional<Manager> findByUserId(UUID userId);

    List<Manager> findAll();

    List<Manager> findAllByOrganizationId(UUID organizationId);

    long countByOrganizationId(UUID organizationId);

    void delete(UUID id);
}
