package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

        ExecutionAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado."));

        if (!executionId.equals(attachment.getProductionExecutionId())) {
            throw new IllegalArgumentException("Anexo não encontrado.");
        }

        attachmentRepository.delete(attachmentId);
    }
}
