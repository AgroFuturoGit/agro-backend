package com.ufal.smartagro.adapters.in.web.dto.production;

import com.ufal.smartagro.domain.model.ExecutionAttachment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadados do anexo. O binário nunca vai no JSON — seria inflar a resposta em
 * base64 para entregar uma lista de miniaturas. Quem quer a imagem segue por
 * `url`.
 */
public record ExecutionAttachmentResponseDTO(
        UUID id,
        UUID productionExecutionId,
        /** Devolvido para que o aparelho reconheça o que ele mesmo enviou. */
        UUID clientId,
        String filename,
        String contentType,
        long sizeBytes,
        String url,
        LocalDateTime createdAt
) {
    /*
     * A conversão mora aqui, e não no `Mapper` compartilhado, de propósito:
     * aquele arquivo já é ponto de conflito entre as branches abertas de
     * produção, e um anexo não precisa atravessá-lo para existir.
     */
    public static ExecutionAttachmentResponseDTO from(ExecutionAttachment attachment) {
        return new ExecutionAttachmentResponseDTO(
                attachment.getId(),
                attachment.getProductionExecutionId(),
                attachment.getClientId(),
                attachment.getFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                "/production-executions/%s/attachments/%s".formatted(
                        attachment.getProductionExecutionId(), attachment.getId()),
                attachment.getCreatedAt()
        );
    }
}
