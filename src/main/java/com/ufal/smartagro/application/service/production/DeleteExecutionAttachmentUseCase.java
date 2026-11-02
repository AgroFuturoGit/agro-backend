package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteExecutionAttachmentUseCase {

    private final ExecutionAttachmentRepository attachmentRepository;
    private final ProductionExecutionRepository executionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID executionId, UUID attachmentId, User loggedUser) {
        ProductionExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Apontamento de colheita não encontrado."));

        accessValidator.validateWriteAccess(
                execution.getProductionPlan() != null ? execution.getProductionPlan().getFarmer() : null,
                loggedUser);

        if (execution.getStatus() == ExecutionStatus.VALIDATED
                && loggedUser != null && loggedUser.getRole() == Role.FARMER) {
            throw new AccessDeniedException("Não é permitido remover comprovações de um apontamento já validado.");
        }

        ExecutionAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado."));

        if (!executionId.equals(attachment.getProductionExecutionId())) {
            throw new IllegalArgumentException("Anexo não encontrado.");
        }

        attachmentRepository.delete(attachmentId);
        log.info("Anexo de apontamento excluído logicamente [attachmentId={}, executionId={}] pelo usuário [actorId={}]",
                attachmentId, executionId, loggedUser.getId());
    }
}
