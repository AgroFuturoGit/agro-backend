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
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Agricultor não encontrado."));

        accessValidator.validateWriteAccess(farmer, loggedUser);

        Harvest harvest = harvestRepository.findById(dto.harvestId())
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Safra não encontrada."));

        Crop crop = cropRepository.findById(dto.cropId())
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Cultivo não encontrado."));

        if (crop.getExpectedProductivity() == null || crop.getExpectedProductivity().signum() <= 0) {
            throw new IllegalArgumentException("O cultivo precisa ter produtividade esperada maior que zero para criar um plano.");
        }

        // Validação de coerência das datas de colheita estimada
        if (dto.expectedHarvestStart() != null && dto.expectedHarvestEnd() != null
                && dto.expectedHarvestStart().isAfter(dto.expectedHarvestEnd())) {
            throw new IllegalArgumentException("A data prevista de início da colheita não pode ser posterior à data de término.");
        }

        // Validação de coerência do plantio
        if (dto.plannedPlantingDate() != null) {
            if (harvest.getStartDate() != null && harvest.getEndDate() != null
                    && (dto.plannedPlantingDate().isBefore(harvest.getStartDate()) || dto.plannedPlantingDate().isAfter(harvest.getEndDate()))) {
                throw new IllegalArgumentException("A data de plantio deve estar dentro do período da safra ("
                        + harvest.getStartDate() + " a " + harvest.getEndDate() + ").");
            }

            if (dto.expectedHarvestStart() != null && dto.plannedPlantingDate().isAfter(dto.expectedHarvestStart())) {
                throw new IllegalArgumentException("A data planejada de plantio não pode ser posterior ao início previsto da colheita.");
            }
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
