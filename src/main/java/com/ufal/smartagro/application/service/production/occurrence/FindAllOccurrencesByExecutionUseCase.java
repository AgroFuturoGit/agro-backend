package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllOccurrencesByExecutionUseCase {

    private final FieldOccurrenceRepository fieldOccurrenceRepository;
    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public List<FieldOccurrence> findAll(UUID executionId, User loggedUser) {
        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateAccess(execution.getProductionPlan().getFarmer(), loggedUser);
        }

        return fieldOccurrenceRepository.findAllByExecutionId(executionId);
    }
}
