package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.FieldOccurrenceMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaFieldOccurrenceRepository;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository @RequiredArgsConstructor
public class FieldOccurrenceRepositoryImpl implements FieldOccurrenceRepository {
    private final JpaFieldOccurrenceRepository jpaRepository;
    private final FieldOccurrenceMapper mapper;
    public FieldOccurrence save(FieldOccurrence value) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(value))); }
    public Optional<FieldOccurrence> findById(UUID id) { return jpaRepository.findById(id).map(mapper::toDomain); }
    public List<FieldOccurrence> findAllByExecutionId(UUID executionId) { return jpaRepository.findAllByExecutionId(executionId).stream().map(mapper::toDomain).toList(); }
    public void delete(UUID id) { jpaRepository.softDeleteById(id); }
}
