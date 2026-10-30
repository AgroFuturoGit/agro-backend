package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comprovação visual anexada a um apontamento de colheita.
 *
 * O conteúdo é opcional neste modelo: a listagem não carrega o binário, que
 * só é lido no download. Carregar todas as fotos para montar uma lista de
 * miniaturas seria trazer megabytes do banco para descartar em seguida.
 */
@Getter
@AllArgsConstructor
public class ExecutionAttachment {
    private UUID id;
    private UUID productionExecutionId;
    /** Identificador gerado no aparelho; deduplica o reenvio da fila offline. */
    private UUID clientId;
    private String filename;
    private String contentType;
    private long sizeBytes;
    private byte[] content;
    private LocalDateTime createdAt;
}
