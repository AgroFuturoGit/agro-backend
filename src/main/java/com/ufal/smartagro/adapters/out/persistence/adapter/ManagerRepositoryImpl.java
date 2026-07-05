package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.ManagerEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.ManagerMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaManagerRepository;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ManagerRepositoryImpl implements ManagerRepository {

    private final JpaManagerRepository jpaRepository;
    private final ManagerMapper mapper;

    @Override
    public Manager save(Manager manager) {
        ManagerEntity entity = mapper.toEntity(manager);
        ManagerEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Manager> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Manager> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public java.util.List<Manager> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.softDeleteById(id);
    }
}
