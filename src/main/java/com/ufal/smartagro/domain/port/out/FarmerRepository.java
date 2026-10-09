package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Farmer;
import java.util.Optional;
import java.util.UUID;

public interface FarmerRepository {
    Farmer save(Farmer farmer);
    Optional<Farmer> findById(UUID id);
    Optional<Farmer> findByUserId(UUID userId);
    Optional<Farmer> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    java.util.List<Farmer> findAll();
    java.util.List<Farmer> findAllByCommunityId(UUID communityId);
    void delete(UUID id);
}
