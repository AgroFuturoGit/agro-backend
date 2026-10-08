package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.ExecutionAttachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionAttachmentRepository {

    ExecutionAttachment save(ExecutionAttachment attachment);

    /** Metadados apenas — sem o binário. */
    List<ExecutionAttachment> findAllByExecutionId(UUID executionId);

    /** Metadados apenas — sem o binário. */
    Optional<ExecutionAttachment> findById(UUID id);

    /** Com o binário, para o download. */
    Optional<ExecutionAttachment> findByIdWithContent(UUID id);

    /** O anexo já gravado para este `clientId`, se a fila já tiver enviado. */
    Optional<ExecutionAttachment> findByExecutionIdAndClientId(UUID executionId, UUID clientId);

    void delete(UUID id);
}
