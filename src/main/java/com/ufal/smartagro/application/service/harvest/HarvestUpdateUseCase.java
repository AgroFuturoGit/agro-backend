package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionVersionGuard;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HarvestUpdateUseCase {

    private final HarvestRepository harvestRepository;
    private final ProductionVersionGuard versionGuard;
    private final AuditLogService auditLogService;

    @Transactional
    public Harvest execute(UUID id, Harvest updatedHarvestData, User loggedUser) {
        return execute(id, updatedHarvestData, loggedUser, null);
    }

    @Transactional
    public Harvest execute(UUID id, Harvest updatedHarvestData, User loggedUser, LocalDateTime baseUpdatedAt) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN)) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar uma safra.");
        }

        Harvest existingHarvest = harvestRepository.findById(id)
                .orElseThrow(HarvestNotFoundException::new);

        versionGuard.ensureUpToDate(
                baseUpdatedAt,
                existingHarvest.getUpdatedAt(),
                existingHarvest.getCreatedAt(),
                existingHarvest,
                "A safra foi alterada por outro usuário depois que esta edição começou."
        );

        if (updatedHarvestData.getStartDate().isAfter(updatedHarvestData.getEndDate())) {
            throw new IllegalArgumentException("A data de início não pode ser superior à data de término.");
        }

        String targetLabel = updatedHarvestData.getLabel() != null ? updatedHarvestData.getLabel().trim() : existingHarvest.getLabel().trim();
        if (targetLabel.isBlank()) {
            throw new IllegalArgumentException("O rótulo da safra não pode ser vazio.");
        }

        boolean isLabelChanged = !existingHarvest.getLabel().trim().equals(targetLabel);
        if (isLabelChanged && harvestRepository.existsByLabel(targetLabel)) {
            throw new HarvestAlreadyExistsException("Já existe uma safra com o rótulo: " + targetLabel);
        }

        if (harvestRepository.existsOverlappingExcludingId(
                updatedHarvestData.getStartDate(), updatedHarvestData.getEndDate(), existingHarvest.getId())) {
            throw new IllegalArgumentException("Já existe uma safra cadastrada com período sobreposto.");
        }

        Harvest harvestToSave = new Harvest(
                existingHarvest.getId(),
                targetLabel,
                updatedHarvestData.getStartDate(),
                updatedHarvestData.getEndDate(),
                existingHarvest.getCreatedAt(),
                existingHarvest.getUpdatedAt(),
                existingHarvest.getDeletedAt()
        );

        Harvest saved = harvestRepository.save(harvestToSave);

        Map<String, Object> details = new HashMap<>();
        details.put("label", saved.getLabel());
        if (saved.getStartDate() != null) {
            details.put("startDate", saved.getStartDate().toString());
        }
        if (saved.getEndDate() != null) {
            details.put("endDate", saved.getEndDate().toString());
        }
        auditLogService.record(loggedUser, AuditAction.UPDATE, "harvests", saved.getId(), details);

        return saved;
    }
}
