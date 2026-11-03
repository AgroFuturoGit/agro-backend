package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
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
public class DeleteFieldOccurrenceUseCase {

    private final FieldOccurrenceRepository fieldOccurrenceRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        FieldOccurrence existing = fieldOccurrenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ocorrência de campo não encontrada."));

        if (existing.getExecution() != null && existing.getExecution().getProductionPlan() != null) {
            accessValidator.validateWriteAccess(existing.getExecution().getProductionPlan().getFarmer(), loggedUser);
        }

        if (existing.getExecution() != null && existing.getExecution().getStatus() == ExecutionStatus.VALIDATED
                && loggedUser.getRole() == Role.FARMER) {
            throw new AccessDeniedException("Não é permitido excluir ocorrências de um apontamento já validado.");
        }

        fieldOccurrenceRepository.delete(id);

        Map<String, Object> details = new HashMap<>();
        details.put("executionId", existing.getExecution() != null ? existing.getExecution().getId() : null);
        details.put("logType", existing.getLogType() != null ? existing.getLogType().name() : null);

        auditLogService.record(loggedUser, AuditAction.DELETE, "field_occurrences", id, details);

        log.info("Ocorrência de campo excluída logicamente [occurrenceId={}] pelo usuário [actorId={}, actorRole={}]",
                id, loggedUser.getId(), loggedUser.getRole());
    }
}
