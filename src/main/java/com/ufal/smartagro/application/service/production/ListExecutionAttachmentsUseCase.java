package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListExecutionAttachmentsUseCase {

    private final ExecutionAttachmentRepository attachmentRepository;
    private final ProductionExecutionRepository executionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public List<ExecutionAttachment> listByExecution(UUID executionId, User loggedUser) {
        ProductionExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Apontamento de colheita não encontrado."));

        accessValidator.validateAccess(
                execution.getProductionPlan() != null ? execution.getProductionPlan().getFarmer() : null,
                loggedUser);

        return attachmentRepository.findAllByExecutionId(executionId);
    }
}
