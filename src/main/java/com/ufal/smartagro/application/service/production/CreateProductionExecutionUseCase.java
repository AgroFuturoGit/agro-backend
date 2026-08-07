package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public ProductionExecution create(UUID planId, ProductionExecutionRegisterDTO dto, User loggedUser) {
        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano de produção não encontrado."));

        accessValidator.validateAccess(plan.getProducer(), loggedUser);

        ProductionExecution execution = new ProductionExecution(
                null,
                plan,
                dto.actualYield(),
                dto.harvestDate(),
                null,
                null,
                null
        );

        return productionExecutionRepository.save(execution);
    }
}
