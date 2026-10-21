package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Financing;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancingRepository {
    Financing save(Financing financing);
    Optional<Financing> findById(UUID id);
    List<Financing> findAllByFarmerId(UUID farmerId);
    void delete(UUID id);
}
