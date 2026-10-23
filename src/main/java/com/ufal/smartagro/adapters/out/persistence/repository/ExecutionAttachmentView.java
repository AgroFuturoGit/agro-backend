package com.ufal.smartagro.adapters.out.persistence.repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projeção com os metadados do anexo, sem o binário.
 *
 * É o que a listagem devolve. Sem ela, montar a lista de miniaturas de um
 * apontamento traria todas as fotos do banco para serem descartadas em
 * seguida pelo serializador.
 */
public interface ExecutionAttachmentView {
    UUID getId();
    UUID getProductionExecutionId();
    UUID getClientId();
    String getFilename();
    String getContentType();
    long getSizeBytes();
    LocalDateTime getCreatedAt();
}
