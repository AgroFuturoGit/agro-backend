package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListProductionExecutionsUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public List<ProductionExecution> listByProductionPlan(UUID planId, User loggedUser) {
        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano de produção não encontrado."));

        accessValidator.validateAccess(plan.getProducer(), loggedUser);

        return productionExecutionRepository.findAllByProductionPlanId(planId);
    }
}
