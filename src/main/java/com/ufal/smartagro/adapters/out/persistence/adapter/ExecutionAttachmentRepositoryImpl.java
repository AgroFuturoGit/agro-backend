package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.ExecutionAttachmentEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.ExecutionAttachmentMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaExecutionAttachmentRepository;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ExecutionAttachmentRepositoryImpl implements ExecutionAttachmentRepository {

    private final JpaExecutionAttachmentRepository jpaRepository;
    private final ExecutionAttachmentMapper mapper;

    @Override
    public ExecutionAttachment save(ExecutionAttachment attachment) {
        var entity = new ExecutionAttachmentEntity();

        // Referência sem carregar o apontamento: a chave estrangeira é tudo
        // de que o insert precisa.
        var execution = new ProductionExecutionEntity();
        execution.setId(attachment.getProductionExecutionId());
        entity.setProductionExecution(execution);

        entity.setClientId(attachment.getClientId());
        entity.setFilename(attachment.getFilename());
        entity.setContentType(attachment.getContentType());
        entity.setSizeBytes(attachment.getSizeBytes());
        entity.setContent(attachment.getContent());

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<ExecutionAttachment> findAllByExecutionId(UUID executionId) {
        return jpaRepository.findMetadataByExecutionId(executionId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ExecutionAttachment> findById(UUID id) {
        return jpaRepository.findMetadataById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ExecutionAttachment> findByIdWithContent(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ExecutionAttachment> findByExecutionIdAndClientId(UUID executionId, UUID clientId) {
        return jpaRepository.findMetadataByExecutionIdAndClientId(executionId, clientId).map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.softDeleteById(id);
    }
}
