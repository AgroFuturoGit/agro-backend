package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.ProducerEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.ProducerMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaProducerRepository;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProducerRepositoryImpl implements ProducerRepository {

    private final JpaProducerRepository jpaRepository;
    private final ProducerMapper mapper;

    @Override
    public Producer save(Producer producer) {
        ProducerEntity entity = mapper.toEntity(producer);
        ProducerEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Producer> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Producer> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public java.util.List<Producer> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.softDeleteById(id);
    }
}
