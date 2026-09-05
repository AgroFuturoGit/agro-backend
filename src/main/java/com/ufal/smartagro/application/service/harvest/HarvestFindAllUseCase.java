package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HarvestFindAllUseCase {

    private final HarvestRepository harvestRepository;

    public List<Harvest> execute(User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN
                && loggedUser.getRole() != Role.TECHNICIAN
                && loggedUser.getRole() != Role.MANAGER
                && loggedUser.getRole() != Role.FARMER) {
            throw new AccessDeniedException("Usuário não tem permissão para visualizar as safras.");
        }
        return harvestRepository.findAll();
    }
}
