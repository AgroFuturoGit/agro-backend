package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/** Registra cada ocorrência de colheita como apontamento independente. */
@Service
@RequiredArgsConstructor
public class CreateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    /** Cria o apontamento em estado PENDING após validar plano e acesso do usuário. */
    public ProductionExecution create(UUID planId, ProductionExecutionRegisterDTO dto, User loggedUser) {
        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Plano de produção não encontrado."));

        accessValidator.validateWriteAccess(plan.getFarmer(), loggedUser);

        LocalDate harvestedAt = dto.harvestedAt();
        if (harvestedAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da colheita não pode ser futura.");
        }
        if (harvestedAt.isBefore(LocalDate.of(1970, 1, 1))) {
            throw new IllegalArgumentException("A data da colheita não pode ser anterior a 1970.");
        }
        if (plan.getPlannedPlantingDate() != null && harvestedAt.isBefore(plan.getPlannedPlantingDate())) {
            throw new IllegalArgumentException("A data da colheita não pode ser anterior à data planejada de plantio.");
        }
        Harvest harvest = plan.getHarvest();
        if (harvest != null && harvest.getStartDate() != null && harvestedAt.isBefore(harvest.getStartDate())) {
            throw new IllegalArgumentException("A data da colheita não pode ser anterior ao início da safra.");
        }

        java.math.BigDecimal quantityKg = ProductionQuantityCalculator.calculateQuantityKg(
                plan.getCrop(),
                dto.quantity(),
                dto.quantityKg()
        );

        ProductionExecution execution = new ProductionExecution(
                null,
                plan,
                dto.quantity(),
                quantityKg,
                harvestedAt,
                com.ufal.smartagro.domain.model.enums.ExecutionStatus.PENDING,
                null,
                null,
                dto.notes(),
                dto.latitude(),
                dto.longitude(),
                dto.locationAccuracy(),
                dto.locationRecordedAt(),
                null,
                null,
                null
        );

        ProductionExecution saved = productionExecutionRepository.save(execution);
        if (plan.getStatus() == com.ufal.smartagro.domain.model.enums.PlanStatus.PLANNED) {
            ProductionPlan activePlan = new ProductionPlan(
                    plan.getId(),
                    plan.getFarmer(),
                    plan.getHarvest(),
                    plan.getCrop(),
                    plan.getPlantedArea(),
                    com.ufal.smartagro.domain.model.enums.PlanStatus.ACTIVE,
                    plan.getExpectedHarvestStart(),
                    plan.getExpectedHarvestEnd(),
                    plan.getExpectedProductivity(),
                    plan.getLocationDescription(),
                    plan.getLatitude(),
                    plan.getLongitude(),
                    plan.getPlannedPlantingDate(),
                    plan.getPlannedCalendar(),
                    plan.getCreatedAt(),
                    null,
                    plan.getDeletedAt()
            );
            productionPlanRepository.save(activePlan);
        }

        return saved;
    }
}
