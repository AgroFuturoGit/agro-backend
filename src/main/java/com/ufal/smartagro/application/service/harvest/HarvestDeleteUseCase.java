package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class HarvestDeleteUseCase {

    private final HarvestRepository harvestRepository;
    private final ProductionPlanRepository productionPlanRepository;

    public void execute(UUID id, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Usuário não tem permissão para deletar uma safra.");
        }

        if (productionPlanRepository.existsByHarvestId(id)) {
            throw new EntityInUseException("Não é possível excluir uma safra que possui planos de produção vinculados.");
        }

        harvestRepository.deleteById(id);
    }
}
