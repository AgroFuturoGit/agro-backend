package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/** Rejeita um apontamento de colheita, associando o revisor e a justificativa da recusa. */
@Service
@RequiredArgsConstructor
public class RejectProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public ProductionExecution reject(UUID executionId, String reason, User loggedUser) {
        if (loggedUser.getRole() == Role.FARMER) {
            throw new AccessDeniedException("O agricultor não tem permissão para rejeitar apontamentos.");
        }

        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(execution.getProductionPlan().getFarmer(), loggedUser);
        }

        String updatedNotes = execution.getNotes();
        if (reason != null && !reason.trim().isEmpty()) {
            String justification = "[Motivo da rejeição]: " + reason.trim();
            updatedNotes = (updatedNotes == null || updatedNotes.trim().isEmpty())
                    ? justification
                    : updatedNotes.trim() + " | " + justification;
        }

        ProductionExecution rejectedExecution = new ProductionExecution(
                execution.getId(),
                execution.getProductionPlan(),
                execution.getQuantity(),
                execution.getQuantityKg(),
                execution.getHarvestedAt(),
                ExecutionStatus.REJECTED,
                loggedUser,
                LocalDateTime.now(),
                updatedNotes,
                execution.getLatitude(),
                execution.getLongitude(),
                execution.getLocationAccuracy(),
                execution.getLocationRecordedAt(),
                execution.getCreatedAt(),
                null,
                execution.getDeletedAt()
        );

        return productionExecutionRepository.save(rejectedExecution);
    }
}
