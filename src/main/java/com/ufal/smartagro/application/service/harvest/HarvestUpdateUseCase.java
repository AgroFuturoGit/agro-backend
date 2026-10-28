package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.production.ProductionVersionGuard;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class HarvestUpdateUseCase {

    private final HarvestRepository harvestRepository;
    private final ProductionVersionGuard versionGuard;

    public HarvestUpdateUseCase(HarvestRepository harvestRepository, ProductionVersionGuard versionGuard) {
        this.harvestRepository = harvestRepository;
        this.versionGuard = versionGuard != null ? versionGuard : new ProductionVersionGuard();
    }

    public HarvestUpdateUseCase(HarvestRepository harvestRepository) {
        this(harvestRepository, new ProductionVersionGuard());
    }

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
                Mapper.toHarvestResponseDTO(existingHarvest),
                "A safra foi alterada por outro usuário depois que esta edição começou."
        );

        if (updatedHarvestData.getStartDate().isAfter(updatedHarvestData.getEndDate())) {
            throw new IllegalArgumentException("A data de início não pode ser superior à data de término.");
        }

        boolean isLabelChanged = !existingHarvest.getLabel().equals(updatedHarvestData.getLabel());
        if (isLabelChanged && harvestRepository.existsByLabel(updatedHarvestData.getLabel())) {
            throw new HarvestAlreadyExistsException("Já existe uma safra com o rótulo: " + updatedHarvestData.getLabel());
        }

        Harvest harvestToSave = new Harvest(
                existingHarvest.getId(),
                updatedHarvestData.getLabel(),
                updatedHarvestData.getStartDate(),
                updatedHarvestData.getEndDate(),
                existingHarvest.getCreatedAt(),
                existingHarvest.getUpdatedAt(),
                existingHarvest.getDeletedAt()
        );

        return harvestRepository.save(harvestToSave);
    }
}
