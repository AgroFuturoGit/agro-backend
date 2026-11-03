package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Atualiza um plano sem substituir o snapshot original de produtividade da cultura. */
@Service
@RequiredArgsConstructor
public class UpdateProductionPlanUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;
    private final ProductionVersionGuard versionGuard;
    private final AuditLogService auditLogService;

    @Transactional
    /** Confere acesso e versão concorrente, aplica os campos enviados e salva o plano. */
    public ProductionPlan update(UUID planId, ProductionPlanUpdateDTO dto, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        ProductionPlan existingPlan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plano de produção não encontrado."));

        accessValidator.validateWriteAccess(existingPlan.getFarmer(), loggedUser);

        versionGuard.ensureUpToDate(
                dto.baseUpdatedAt(),
                existingPlan.getUpdatedAt(),
                existingPlan.getCreatedAt(),
                existingPlan,
                "O plano de produção foi alterado por outro usuário depois que esta edição começou."
        );

        LocalDate harvestStart = Boolean.TRUE.equals(dto.clearHarvestDates()) ? null
                : (dto.expectedHarvestStart() != null ? dto.expectedHarvestStart() : existingPlan.getExpectedHarvestStart());
        LocalDate harvestEnd = Boolean.TRUE.equals(dto.clearHarvestDates()) ? null
                : (dto.expectedHarvestEnd() != null ? dto.expectedHarvestEnd() : existingPlan.getExpectedHarvestEnd());

        LocalDate plantingDate = Boolean.TRUE.equals(dto.clearPlannedPlantingDate()) ? null
                : (dto.plannedPlantingDate() != null ? dto.plannedPlantingDate() : existingPlan.getPlannedPlantingDate());

        // Validação de coerência das datas de colheita estimada
        if (harvestStart != null && harvestEnd != null && harvestStart.isAfter(harvestEnd)) {
            throw new IllegalArgumentException("A data prevista de início da colheita não pode ser posterior à data de término.");
        }

        // Validação de coerência do plantio
        if (plantingDate != null) {
            Harvest harvest = existingPlan.getHarvest();
            if (harvest != null && harvest.getStartDate() != null && harvest.getEndDate() != null
                    && (plantingDate.isBefore(harvest.getStartDate()) || plantingDate.isAfter(harvest.getEndDate()))) {
                throw new IllegalArgumentException("A data de plantio deve estar dentro do período da safra ("
                        + harvest.getStartDate() + " a " + harvest.getEndDate() + ").");
            }

            if (harvestStart != null && plantingDate.isAfter(harvestStart)) {
                throw new IllegalArgumentException("A data planejada de plantio não pode ser posterior ao início previsto da colheita.");
            }
        }

        String locationDesc = existingPlan.getLocationDescription();
        if (dto.locationDescription() != null) {
            String trimmed = dto.locationDescription().trim();
            locationDesc = trimmed.isEmpty() ? null : trimmed;
        }

        ProductionPlan updatedPlan = new ProductionPlan(
                existingPlan.getId(),
                existingPlan.getClientId(),
                existingPlan.getFarmer(),
                existingPlan.getHarvest(),
                existingPlan.getCrop(),
                dto.plantedArea() != null ? dto.plantedArea() : existingPlan.getPlantedArea(),
                dto.status() != null ? dto.status() : existingPlan.getStatus(),
                harvestStart,
                harvestEnd,
                existingPlan.getExpectedProductivity(),
                locationDesc,
                dto.latitude() != null ? dto.latitude() : existingPlan.getLatitude(),
                dto.longitude() != null ? dto.longitude() : existingPlan.getLongitude(),
                plantingDate,
                dto.plannedCalendar() != null ? dto.plannedCalendar() : existingPlan.getPlannedCalendar(),
                existingPlan.getCreatedAt(),
                LocalDateTime.now(),
                existingPlan.getDeletedAt()
        );

        ProductionPlan saved = productionPlanRepository.save(updatedPlan);

        Map<String, Object> details = new HashMap<>();
        details.put("plantedArea", saved.getPlantedArea());
        details.put("status", saved.getStatus() != null ? saved.getStatus().name() : null);

        auditLogService.record(loggedUser, AuditAction.UPDATE, "production_plans", saved.getId(), details);

        return saved;
    }
}
