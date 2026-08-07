package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public ProductionExecution update(UUID executionId, ProductionExecutionUpdateDTO dto, User loggedUser) {
        ProductionExecution existingExecution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));

        if (existingExecution.getProductionPlan() != null) {
            accessValidator.validateAccess(existingExecution.getProductionPlan().getProducer(), loggedUser);
        }

        ProductionExecution updatedExecution = new ProductionExecution(
                existingExecution.getId(),
                existingExecution.getProductionPlan(),
                dto.actualYield() != null ? dto.actualYield() : existingExecution.getActualYield(),
                dto.harvestDate() != null ? dto.harvestDate() : existingExecution.getHarvestDate(),
                existingExecution.getCreatedAt(),
                null,
                existingExecution.getDeletedAt()
        );

        return productionExecutionRepository.save(updatedExecution);
    }
}
