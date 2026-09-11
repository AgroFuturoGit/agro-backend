package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class HarvestFindByIdUseCase {

    private final HarvestRepository harvestRepository;

    public Harvest execute(UUID id, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN
                && loggedUser.getRole() != Role.TECHNICIAN
                && loggedUser.getRole() != Role.MANAGER
                && loggedUser.getRole() != Role.FARMER) {
            throw new AccessDeniedException("Usuário não tem permissão para visualizar uma safra.");
        }
        return harvestRepository.findById(id)
                .orElseThrow(HarvestNotFoundException::new);
    }
}
