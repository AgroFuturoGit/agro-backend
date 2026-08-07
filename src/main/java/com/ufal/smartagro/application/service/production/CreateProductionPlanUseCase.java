package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanRegisterDTO;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateProductionPlanUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProducerRepository producerRepository;
    private final HarvestRepository harvestRepository;
    private final CropRepository cropRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public ProductionPlan create(UUID producerId, ProductionPlanRegisterDTO dto, User loggedUser) {
        Producer producer = producerRepository.findById(producerId)
                .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));

        accessValidator.validateAccess(producer, loggedUser);

        Harvest harvest = harvestRepository.findById(dto.harvestId())
                .orElseThrow(() -> new IllegalArgumentException("Safra não encontrada."));

        Crop crop = cropRepository.findById(dto.cropId())
                .orElseThrow(() -> new IllegalArgumentException("Cultivo não encontrado."));

        ProductionPlan plan = new ProductionPlan(
                null,
                producer,
                harvest,
                crop,
                dto.plantedArea(),
                dto.expectedYield(),
                dto.plannedPlantingDate(),
                dto.plannedCalendar(),
                null,
                null,
                null
        );

        return productionPlanRepository.save(plan);
    }
}
