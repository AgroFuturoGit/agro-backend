package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.application.service.production.ProductionVersionGuard;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HarvestUpdateUseCase {

    private final HarvestRepository harvestRepository;
    private final ProductionVersionGuard versionGuard;

    @Transactional
    public Harvest execute(UUID id, Harvest updatedHarvestData, User loggedUser) {
        return execute(id, updatedHarvestData, loggedUser, null);
    }

    @Transactional
    public Harvest execute(UUID id, Harvest updatedHarvestData, User loggedUser, LocalDateTime baseUpdatedAt) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
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

        boolean hasOverlap = harvestRepository.findAll().stream()
                .filter(existing -> existing.getId() != null && !existing.getId().equals(existingHarvest.getId()))
                .anyMatch(existing -> existing.getStartDate() != null && existing.getEndDate() != null
                        && !updatedHarvestData.getEndDate().isBefore(existing.getStartDate())
                        && !updatedHarvestData.getStartDate().isAfter(existing.getEndDate()));
        if (hasOverlap) {
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

        return harvestRepository.save(harvestToSave);
    }
}
