package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceRegisterDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterFieldOccurrenceUseCase {

    private final FieldOccurrenceRepository fieldOccurrenceRepository;
    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public FieldOccurrence register(UUID executionId, FieldOccurrenceRegisterDTO dto, User loggedUser) {
        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(execution.getProductionPlan().getFarmer(), loggedUser);
        }

        FieldOccurrence occurrence = FieldOccurrence.builder()
                .execution(execution)
                .logType(dto.logType())
                .description(dto.description().trim())
                .weatherObservation(dto.weatherObservation() != null ? dto.weatherObservation().trim() : null)
                .evidenceUrl(dto.evidenceUrl() != null ? dto.evidenceUrl().trim() : null)
                .build();

        return fieldOccurrenceRepository.save(occurrence);
    }
}
