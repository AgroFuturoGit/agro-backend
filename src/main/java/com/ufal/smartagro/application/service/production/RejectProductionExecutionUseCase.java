package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Rejeita um apontamento de colheita, associando o revisor e a justificativa da recusa. */
@Service
@RequiredArgsConstructor
public class RejectProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public ProductionExecution reject(UUID executionId, String reason, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() == Role.FARMER) {
            throw new AccessDeniedException("O agricultor não tem permissão para rejeitar apontamentos.");
        }

        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(execution.getProductionPlan().getFarmer(), loggedUser);
        }

        ExecutionStatus oldStatus = execution.getStatus();
        String rejectionReason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : null;

        ProductionExecution rejectedExecution = new ProductionExecution(
                execution.getId(),
                execution.getProductionPlan(),
                execution.getQuantity(),
                execution.getQuantityKg(),
                execution.getHarvestedAt(),
                ExecutionStatus.REJECTED,
                loggedUser,
                LocalDateTime.now(),
                execution.getNotes(),
                rejectionReason,
                execution.getLatitude(),
                execution.getLongitude(),
                execution.getLocationAccuracy(),
                execution.getLocationRecordedAt(),
                execution.getCreatedAt(),
                null,
                execution.getDeletedAt()
        );

        ProductionExecution saved = productionExecutionRepository.save(rejectedExecution);

        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", oldStatus != null ? oldStatus.name() : null);
        details.put("reason", saved.getRejectionReason());

        auditLogService.record(
                loggedUser,
                AuditAction.REJECT,
                "production_executions",
                saved.getId(),
                details
        );

        return saved;
    }
}
