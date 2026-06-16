package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;

    @Transactional
    public ProductionExecution update(UUID executionId, ProductionExecutionUpdateDTO dto) {
        ProductionExecution existingExecution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));

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
