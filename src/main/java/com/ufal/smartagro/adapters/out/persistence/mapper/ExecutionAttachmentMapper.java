package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ExecutionAttachmentEntity;
import com.ufal.smartagro.adapters.out.persistence.repository.ExecutionAttachmentView;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import org.springframework.stereotype.Component;

@Component
public class ExecutionAttachmentMapper {

    /** Do registro completo, com o binário — usado no download. */
    public ExecutionAttachment toDomain(ExecutionAttachmentEntity entity) {
        if (entity == null) return null;
        return new ExecutionAttachment(
                entity.getId(),
                entity.getProductionExecution() != null ? entity.getProductionExecution().getId() : null,
                entity.getClientId(),
                entity.getFilename(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }

    /** Da projeção, sem o binário — usado na listagem. */
    public ExecutionAttachment toDomain(ExecutionAttachmentView view) {
        if (view == null) return null;
        return new ExecutionAttachment(
                view.getId(),
                view.getProductionExecutionId(),
                view.getClientId(),
                view.getFilename(),
                view.getContentType(),
                view.getSizeBytes(),
                null,
                view.getCreatedAt()
        );
    }
}
