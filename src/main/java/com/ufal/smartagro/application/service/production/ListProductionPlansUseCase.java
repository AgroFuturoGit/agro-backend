package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListProductionPlansUseCase {

    private final ProductionPlanRepository productionPlanRepository;

    @Transactional(readOnly = true)
    public List<ProductionPlan> listByProducer(UUID producerId) {
        return productionPlanRepository.findAllByProducerId(producerId);
    }
}
