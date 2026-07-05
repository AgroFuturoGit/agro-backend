package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindProductionPlanByIdUseCase {

    private final ProductionPlanRepository productionPlanRepository;

    @Transactional(readOnly = true)
    public ProductionPlan findById(UUID id) {
        return productionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plano de produção não encontrado."));
    }
}
