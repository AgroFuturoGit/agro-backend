package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Farmer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FarmerRepository {
    Farmer save(Farmer farmer);

    Optional<Farmer> findById(UUID id);

    Optional<Farmer> findByUserId(UUID userId);

    Optional<Farmer> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Farmer> findAll();

    List<Farmer> findAllByCommunityId(UUID communityId);

    List<Farmer> findAllByOrganizationId(UUID organizationId);

    List<Farmer> findAllByCommunityIdIn(Collection<UUID> communityIds);

    void delete(UUID id);
}
