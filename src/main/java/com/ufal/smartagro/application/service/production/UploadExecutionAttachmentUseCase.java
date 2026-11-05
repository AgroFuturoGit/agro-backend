package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadExecutionAttachmentUseCase {

    /** Espelha o CHECK da migration V18. */
    static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    /**
     * Teto de anexos por apontamento.
     *
     * Não é limitação técnica, é contenção de volume: com o binário no banco,
     * um apontamento sem teto vira um caminho aberto para encher o disco do
     * Postgres com fotos.
     */
    static final int MAX_ATTACHMENTS_PER_EXECUTION = 5;

    private final ExecutionAttachmentRepository attachmentRepository;
    private final ProductionExecutionRepository executionRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public AttachmentUploadResult upload(UUID executionId, NewExecutionAttachment incoming, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        ProductionExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Apontamento de colheita não encontrado."));

        accessValidator.validateWriteAccess(
                execution.getProductionPlan() != null ? execution.getProductionPlan().getFarmer() : null,
                loggedUser);

        if (execution.getStatus() == ExecutionStatus.VALIDATED && loggedUser.getRole() == Role.FARMER) {
            throw new AccessDeniedException("Não é permitido anexar comprovações a um apontamento já validado.");
        }

        /*
         * O reenvio da fila offline chega aqui como uma requisição idêntica à
         * primeira. Devolver o anexo que já existe — em vez de gravar outro —
         * é o que torna o upload seguro para repetir: o aparelho perde a
         * resposta, tenta de novo e recebe o mesmo registro de volta.
         */
        if (incoming.clientId() != null) {
            var existing = attachmentRepository
                    .findByExecutionIdAndClientId(executionId, incoming.clientId());
            if (existing.isPresent()) {
                return new AttachmentUploadResult(existing.get(), false);
            }
        }

        validate(incoming);

        if (attachmentRepository.countByExecutionId(executionId) >= MAX_ATTACHMENTS_PER_EXECUTION) {
            throw new IllegalArgumentException(
                    "Este apontamento já tem " + MAX_ATTACHMENTS_PER_EXECUTION + " anexos, que é o máximo permitido.");
        }

        ExecutionAttachment attachment = new ExecutionAttachment(
                null,
                executionId,
                incoming.clientId(),
                incoming.filename(),
                incoming.contentType(),
                incoming.content().length,
                incoming.content(),
                null
        );

        ExecutionAttachment saved = attachmentRepository.save(attachment);

        Map<String, Object> details = new HashMap<>();
        details.put("executionId", executionId);
        details.put("filename", saved.getFilename());
        details.put("contentType", saved.getContentType());
        details.put("sizeBytes", saved.getSizeBytes());

        auditLogService.record(loggedUser, AuditAction.CREATE, "execution_attachments", saved.getId(), details);

        return new AttachmentUploadResult(saved, true);
    }

    private void validate(NewExecutionAttachment incoming) {
        if (incoming.content() == null || incoming.content().length == 0) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }

        if (incoming.content().length > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("A imagem excede o limite de 5 MB.");
        }

        if (incoming.contentType() == null || !ALLOWED_CONTENT_TYPES.contains(incoming.contentType())) {
            throw new IllegalArgumentException("Formato não aceito. Envie a imagem como JPEG, PNG ou WebP.");
        }
    }
}
