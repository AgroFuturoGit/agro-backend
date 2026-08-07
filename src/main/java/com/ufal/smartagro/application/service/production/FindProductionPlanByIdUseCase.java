package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindProductionPlanByIdUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public ProductionPlan findById(UUID id, User loggedUser) {
        ProductionPlan plan = productionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano de produção não encontrado."));

        accessValidator.validateAccess(plan.getProducer(), loggedUser);

        return plan;
    }
}
