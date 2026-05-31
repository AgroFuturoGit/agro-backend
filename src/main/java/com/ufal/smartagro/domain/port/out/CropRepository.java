package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Crop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CropRepository {
    Crop save(Crop crop);

    boolean existsByNameAndVariety(String name, String variety);

    Optional<Crop> findById(UUID id);

    List<Crop> findAll();
}
