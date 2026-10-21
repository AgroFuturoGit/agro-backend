package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.FamilyComposition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FamilyCompositionRepository {
    FamilyComposition save(FamilyComposition familyComposition);
    Optional<FamilyComposition> findById(UUID id);
    List<FamilyComposition> findAllByFarmerId(UUID farmerId);
    void delete(UUID id);
}
