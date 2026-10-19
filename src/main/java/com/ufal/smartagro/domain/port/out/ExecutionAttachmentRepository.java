package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.ExecutionAttachment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionAttachmentRepository {
    ExecutionAttachment save(ExecutionAttachment attachment);
    Optional<ExecutionAttachment> findById(UUID id);
    List<ExecutionAttachment> findAllByExecutionId(UUID executionId);
    void delete(UUID id);
}
