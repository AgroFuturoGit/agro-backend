package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Community;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityRepository {
    Community save(Community community);

    Optional<Community> findById(UUID id);

    List<Community> findAll();

    List<Community> findAllByOrganizationId(UUID organizationId);

    long countByOrganizationId(UUID organizationId);

    void delete(UUID id);
}
