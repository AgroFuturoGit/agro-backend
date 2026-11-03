package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Registra cada ocorrência de colheita como apontamento independente. */
@Service
@RequiredArgsConstructor
public class CreateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    /** Cria o apontamento em estado PENDING após validar plano e acesso do usuário. */
    public ProductionExecution create(UUID planId, ProductionExecutionRegisterDTO dto, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plano de produção não encontrado."));

        accessValidator.validateWriteAccess(plan.getFarmer(), loggedUser);

        if (dto.clientId() != null) {
            var existing = productionExecutionRepository.findByPlanIdAndClientId(planId, dto.clientId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

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
                dto.clientId(),
                plan,
                dto.quantity(),
                quantityKg,
                harvestedAt,
                com.ufal.smartagro.domain.model.enums.ExecutionStatus.PENDING,
                null,
                null,
                dto.notes(),
                null,
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
                    plan.getClientId(),
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

        Map<String, Object> details = new HashMap<>();
        details.put("productionPlanId", plan.getId());
        details.put("quantityKg", saved.getQuantityKg());
        details.put("harvestedAt", saved.getHarvestedAt() != null ? saved.getHarvestedAt().toString() : null);
        details.put("status", saved.getStatus() != null ? saved.getStatus().name() : null);

        auditLogService.record(loggedUser, AuditAction.CREATE, "production_executions", saved.getId(), details);

        return saved;
    }
}
