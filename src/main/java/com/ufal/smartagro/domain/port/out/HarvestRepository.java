package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Harvest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HarvestRepository {

    Harvest save(Harvest harvest);

    Optional<Harvest> findById(UUID id);

    List<Harvest> findAll();

    boolean existsByLabel(String label);

    boolean existsByStartDateAndEndDate(
            LocalDate startDate,
            LocalDate endDate
    );

    void deleteById(UUID id);
}
