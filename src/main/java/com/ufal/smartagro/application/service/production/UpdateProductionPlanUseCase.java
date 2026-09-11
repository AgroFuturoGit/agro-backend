package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProductionPlanUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;
    private final ProductionVersionGuard versionGuard;

    @Transactional
    public ProductionPlan update(UUID planId, ProductionPlanUpdateDTO dto, User loggedUser) {
        ProductionPlan existingPlan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano de produção não encontrado."));

        accessValidator.validateAccess(existingPlan.getFarmer(), loggedUser);

        versionGuard.ensureUpToDate(
                dto.baseUpdatedAt(),
                existingPlan.getUpdatedAt(),
                existingPlan.getCreatedAt(),
                Mapper.toProductionPlanResponseDTO(existingPlan),
                "O plano de produção foi alterado por outro usuário depois que esta edição começou."
        );

        ProductionPlan updatedPlan = new ProductionPlan(
                existingPlan.getId(),
                existingPlan.getFarmer(),
                existingPlan.getHarvest(),
                existingPlan.getCrop(),
                dto.plantedArea() != null ? dto.plantedArea() : existingPlan.getPlantedArea(),
                dto.expectedYield() != null ? dto.expectedYield() : existingPlan.getExpectedYield(),
                dto.plannedPlantingDate() != null ? dto.plannedPlantingDate() : existingPlan.getPlannedPlantingDate(),
                dto.plannedCalendar() != null ? dto.plannedCalendar() : existingPlan.getPlannedCalendar(),
                existingPlan.getCreatedAt(),
                null,
                existingPlan.getDeletedAt()
        );

        return productionPlanRepository.save(updatedPlan);
    }
}
