package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterFieldOccurrenceUseCase {

    private final FieldOccurrenceRepository fieldOccurrenceRepository;
    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public FieldOccurrence register(UUID executionId, FieldOccurrenceRegisterDTO dto, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(execution.getProductionPlan().getFarmer(), loggedUser);
        }

        if (execution.getStatus() == ExecutionStatus.VALIDATED && loggedUser.getRole() == Role.FARMER) {
            throw new AccessDeniedException("Não é permitido registrar ocorrências em um apontamento já validado.");
        }

        if (dto.clientId() != null) {
            var existing = fieldOccurrenceRepository.findByExecutionIdAndClientId(executionId, dto.clientId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        FieldOccurrence occurrence = FieldOccurrence.builder()
                .clientId(dto.clientId())
                .execution(execution)
                .logType(dto.logType())
                .description(dto.description().trim())
                .weatherObservation(dto.weatherObservation() != null ? dto.weatherObservation().trim() : null)
                .evidenceUrl(dto.evidenceUrl() != null ? dto.evidenceUrl().trim() : null)
                .build();

        FieldOccurrence saved = fieldOccurrenceRepository.save(occurrence);

        Map<String, Object> details = new HashMap<>();
        details.put("executionId", executionId);
        details.put("logType", saved.getLogType() != null ? saved.getLogType().name() : null);

        auditLogService.record(loggedUser, AuditAction.CREATE, "field_occurrences", saved.getId(), details);

        return saved;
    }
}
