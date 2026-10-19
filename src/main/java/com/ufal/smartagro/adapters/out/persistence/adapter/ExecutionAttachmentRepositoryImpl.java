package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.ExecutionAttachmentMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaExecutionAttachmentRepository;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository @RequiredArgsConstructor
public class ExecutionAttachmentRepositoryImpl implements ExecutionAttachmentRepository {
    private final JpaExecutionAttachmentRepository jpaRepository;
    private final ExecutionAttachmentMapper mapper;
    public ExecutionAttachment save(ExecutionAttachment value) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(value))); }
    public Optional<ExecutionAttachment> findById(UUID id) { return jpaRepository.findById(id).map(mapper::toDomain); }
    public List<ExecutionAttachment> findAllByExecutionId(UUID executionId) { return jpaRepository.findAllByExecutionId(executionId).stream().map(mapper::toDomain).toList(); }
    public void delete(UUID id) { jpaRepository.softDeleteById(id); }
}
