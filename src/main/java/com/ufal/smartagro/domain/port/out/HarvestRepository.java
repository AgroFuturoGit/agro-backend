package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Harvest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HarvestRepository {

    Harvest save(Harvest harvest);

    Optional<Harvest> findById(UUID id);

    List<Harvest> findAll();

    boolean existsByLabel(String label);

    void deleteById(UUID id);
}
