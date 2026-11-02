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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID executionId, User loggedUser) {
        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(execution.getProductionPlan().getFarmer(), loggedUser);
        }

        if (execution.getStatus() == ExecutionStatus.VALIDATED) {
            if (loggedUser != null && loggedUser.getRole() == Role.FARMER) {
                throw new AccessDeniedException("O agricultor não tem permissão para excluir um apontamento já validado.");
            }

            if (loggedUser != null) {
                Map<String, Object> details = new HashMap<>();
                details.put("status", execution.getStatus().name());
                details.put("quantityKg", execution.getQuantityKg());
                details.put("harvestedAt", execution.getHarvestedAt() != null ? execution.getHarvestedAt().toString() : null);

                auditLogService.record(
                        loggedUser,
                        AuditAction.DELETE,
                        "production_executions",
                        executionId,
                        details
                );
            }
        }

        productionExecutionRepository.delete(executionId);
        log.info("Apontamento de produção excluído logicamente [executionId={}] pelo usuário [actorId={}, actorRole={}]",
                executionId, loggedUser != null ? loggedUser.getId() : "system", loggedUser != null ? loggedUser.getRole() : "UNKNOWN");
    }
}
