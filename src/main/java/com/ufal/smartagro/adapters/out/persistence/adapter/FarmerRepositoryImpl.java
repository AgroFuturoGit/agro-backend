package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.FarmerEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.FarmerMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaFarmerRepository;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FarmerRepositoryImpl implements FarmerRepository {

    private final JpaFarmerRepository jpaRepository;
    private final FarmerMapper mapper;

    @Override
    public Farmer save(Farmer farmer) {
        FarmerEntity entity = mapper.toEntity(farmer);
        FarmerEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Farmer> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Farmer> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Farmer> findByCpf(String cpf) {
        return jpaRepository.findByCpf(cpf).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }

    @Override
    public List<Farmer> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Farmer> findAllByCommunityId(UUID communityId) {
        return jpaRepository.findAllByCommunityId(communityId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Farmer> findAllByOrganizationId(UUID organizationId) {
        return jpaRepository.findAllByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Farmer> findAllByCommunityIdIn(Collection<UUID> communityIds) {
        if (communityIds == null || communityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jpaRepository.findAllByCommunityIdIn(communityIds).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.softDeleteById(id);
    }
}
