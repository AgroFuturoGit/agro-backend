package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListProductionExecutionsUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;

    @Transactional(readOnly = true)
    public List<ProductionExecution> listByProductionPlan(UUID planId) {
        return productionExecutionRepository.findAllByProductionPlanId(planId);
    }
}
