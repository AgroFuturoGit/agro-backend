package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID executionId, User loggedUser) {
        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateAccess(execution.getProductionPlan().getProducer(), loggedUser);
        }

        productionExecutionRepository.delete(executionId);
    }
}
