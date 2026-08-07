package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
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
    private final ProducerRepository producerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public List<ProductionPlan> listByProducer(UUID producerId, User loggedUser) {
        Producer producer = producerRepository.findById(producerId)
                .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));

        accessValidator.validateAccess(producer, loggedUser);

        return productionPlanRepository.findAllByProducerId(producerId);
    }
}
