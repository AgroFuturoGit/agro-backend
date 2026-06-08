package com.ufal.smartagro.application.service.harvest;

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

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class HarvestUpdateUseCase {

    private final HarvestRepository harvestRepository;

    @Transactional
    public Harvest execute(UUID id, Harvest updatedHarvestData, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar uma safra.");
        }

        Harvest existingHarvest = harvestRepository.findById(id)
                .orElseThrow(HarvestNotFoundException::new);

        boolean isLabelChanged = !existingHarvest.getLabel().equals(updatedHarvestData.getLabel());
        if (isLabelChanged && harvestRepository.existsByLabel(updatedHarvestData.getLabel())) {
            throw new HarvestAlreadyExistsException("Já existe uma safra com o rótulo: " + updatedHarvestData.getLabel());
        }

        boolean isDatesChanged = !existingHarvest.getStartDate().equals(updatedHarvestData.getStartDate()) ||
                !existingHarvest.getEndDate().equals(updatedHarvestData.getEndDate());
        if (isDatesChanged && harvestRepository.existsByStartDateAndEndDate(updatedHarvestData.getStartDate(), updatedHarvestData.getEndDate())) {
            throw new HarvestAlreadyExistsException("Já existe uma safra com o mesmo período.");
        }

        Harvest harvestToSave = new Harvest(
                existingHarvest.getId(),
                updatedHarvestData.getLabel(),
                updatedHarvestData.getStartDate(),
                updatedHarvestData.getEndDate()
        );

        return harvestRepository.save(harvestToSave);
    }
}
