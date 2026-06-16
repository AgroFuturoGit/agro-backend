package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteProductionPlanUseCase {

    private final ProductionPlanRepository productionPlanRepository;

    @Transactional
    public void delete(UUID planId) {
        productionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano de produção não encontrado."));
        
        productionPlanRepository.delete(planId);
    }
}
