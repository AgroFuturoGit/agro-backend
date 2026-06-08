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

        if (harvestRepository.existsByLabel(harvest.getLabel())) {
            throw new HarvestAlreadyExistsException("Já existe uma safra com o rótulo: " + harvest.getLabel());
        }

        if (harvestRepository.existsByStartDateAndEndDate(harvest.getStartDate(), harvest.getEndDate())) {
            throw new HarvestAlreadyExistsException("Já existe uma safra com o mesmo período.");
        }

        return harvestRepository.save(harvest);
    }
}
