package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class HarvestRegisterUseCase {

    private final HarvestRepository harvestRepository;

    @Transactional
    public Harvest execute(Harvest harvest, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Usuário não tem permissão para registar uma safra.");
        }

        if (harvest.getStartDate().isAfter(harvest.getEndDate())) {
            throw new IllegalArgumentException("A data de início não pode ser superior à data de término.");
        }

        String trimmedLabel = harvest.getLabel() != null ? harvest.getLabel().trim() : null;
        if (trimmedLabel == null || trimmedLabel.isBlank()) {
            throw new IllegalArgumentException("O rótulo da safra não pode ser vazio.");
        }

        if (harvestRepository.existsByLabel(trimmedLabel)) {
            throw new HarvestAlreadyExistsException("Já existe uma safra com o rótulo: " + trimmedLabel);
        }

        boolean hasOverlap = harvestRepository.findAll().stream()
                .anyMatch(existing -> existing.getStartDate() != null && existing.getEndDate() != null
                        && !harvest.getEndDate().isBefore(existing.getStartDate())
                        && !harvest.getStartDate().isAfter(existing.getEndDate()));
        if (hasOverlap) {
            throw new IllegalArgumentException("Já existe uma safra cadastrada com período sobreposto.");
        }

        Harvest harvestToSave = new Harvest(
                harvest.getId(),
                trimmedLabel,
                harvest.getStartDate(),
                harvest.getEndDate()
        );

        return harvestRepository.save(harvestToSave);
    }
}
