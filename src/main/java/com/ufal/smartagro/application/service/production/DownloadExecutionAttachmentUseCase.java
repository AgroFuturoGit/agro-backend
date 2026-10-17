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
public class DownloadExecutionAttachmentUseCase {

    private final ExecutionAttachmentRepository attachmentRepository;
    private final ProductionExecutionRepository executionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public ExecutionAttachment download(UUID executionId, UUID attachmentId, User loggedUser) {
        ProductionExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Apontamento de colheita não encontrado."));

        accessValidator.validateAccess(
                execution.getProductionPlan() != null ? execution.getProductionPlan().getFarmer() : null,
                loggedUser);

        ExecutionAttachment attachment = attachmentRepository.findByIdWithContent(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Anexo não encontrado."));

        // Sem esta checagem, conhecer o id de um anexo bastaria para baixá-lo
        // por meio de um apontamento a que se tem acesso.
        if (!executionId.equals(attachment.getProductionExecutionId())) {
            throw new IllegalArgumentException("Anexo não encontrado.");
        }

        return attachment;
    }
}
