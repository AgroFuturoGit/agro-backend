package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Manager;
import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository {
    Manager save(Manager manager);
    Optional<Manager> findById(UUID id);
}
