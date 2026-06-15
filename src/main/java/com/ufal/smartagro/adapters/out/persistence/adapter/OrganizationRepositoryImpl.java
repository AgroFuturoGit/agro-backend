package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.OrganizationEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.OrganizationMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaOrganizationRepository;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private final JpaOrganizationRepository jpaRepository;
    private final OrganizationMapper mapper;

    @Override
    public Organization save(Organization organization) {
        OrganizationEntity entity = mapper.toEntity(organization);
        OrganizationEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Organization> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return jpaRepository.existsByTaxId(taxId);
    }
}
