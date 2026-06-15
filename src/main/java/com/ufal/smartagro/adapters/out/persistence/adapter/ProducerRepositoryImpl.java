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
}
