package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ExecutionAttachmentEntity;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class ExecutionAttachmentMapper {
    private final ProductionExecutionMapper executionMapper;
    private final UserMapper userMapper;
    public ExecutionAttachment toDomain(ExecutionAttachmentEntity entity) {
        if (entity == null) return null;
        return ExecutionAttachment.builder().id(entity.getId()).execution(executionMapper.toDomain(entity.getExecution()))
                .fileUrl(entity.getFileUrl()).mimeType(entity.getMimeType()).fileSize(entity.getFileSize())
                .createdBy(userMapper.toDomain(entity.getCreatedBy())).createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt()).deletedAt(entity.getDeletedAt()).build();
    }
    public ExecutionAttachmentEntity toEntity(ExecutionAttachment domain) {
        if (domain == null) return null;
        ExecutionAttachmentEntity entity = new ExecutionAttachmentEntity();
        entity.setId(domain.getId()); entity.setExecution(executionMapper.toEntity(domain.getExecution())); entity.setFileUrl(domain.getFileUrl());
        entity.setMimeType(domain.getMimeType()); entity.setFileSize(domain.getFileSize()); entity.setCreatedBy(userMapper.toEntity(domain.getCreatedBy()));
        entity.setCreatedAt(domain.getCreatedAt()); entity.setUpdatedAt(domain.getUpdatedAt()); entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
