package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanRegisterDTO;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Cria plano de produção com snapshot da produtividade da cultura e dados do talhão. */
@Service
@RequiredArgsConstructor
public class CreateProductionPlanUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final FarmerRepository farmerRepository;
    private final HarvestRepository harvestRepository;
    private final CropRepository cropRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    /** Valida vínculos e autorização antes de persistir o novo planejamento. */
    public ProductionPlan create(UUID farmerId, ProductionPlanRegisterDTO dto, User loggedUser) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));

        accessValidator.validateWriteAccess(farmer, loggedUser);

        Harvest harvest = harvestRepository.findById(dto.harvestId())
                .orElseThrow(() -> new IllegalArgumentException("Safra não encontrada."));

        Crop crop = cropRepository.findById(dto.cropId())
                .orElseThrow(() -> new IllegalArgumentException("Cultivo não encontrado."));

        if (crop.getExpectedProductivity() == null || crop.getExpectedProductivity().signum() <= 0) {
            throw new IllegalArgumentException("O cultivo precisa ter produtividade esperada maior que zero para criar um plano.");
        }

        ProductionPlan plan = new ProductionPlan(
                null,
                farmer,
                harvest,
                crop,
                dto.plantedArea(),
                com.ufal.smartagro.domain.model.enums.PlanStatus.PLANNED,
                dto.expectedHarvestStart(), dto.expectedHarvestEnd(), crop.getExpectedProductivity(),
                dto.locationDescription(), dto.latitude(), dto.longitude(),
                dto.plannedPlantingDate(),
                dto.plannedCalendar(),
                null,
                null,
                null
        );

        return productionPlanRepository.save(plan);
    }
}
