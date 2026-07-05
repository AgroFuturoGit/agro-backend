package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Organization;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findById(UUID id);
    List<Organization> findAll();
    boolean existsByTaxId(String taxId);
    void delete(UUID id);
}
