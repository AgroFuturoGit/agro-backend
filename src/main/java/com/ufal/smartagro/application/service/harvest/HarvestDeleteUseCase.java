package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class HarvestDeleteUseCase {

    private final HarvestRepository harvestRepository;
    private final ProductionPlanRepository productionPlanRepository;

    @Transactional
    public void execute(UUID id, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Usuário não tem permissão para deletar uma safra.");
        }

        harvestRepository.findById(id)
                .orElseThrow(HarvestNotFoundException::new);

        long planCount = productionPlanRepository.countByHarvestId(id);
        if (planCount > 0 || productionPlanRepository.existsByHarvestId(id)) {
            long effectiveCount = planCount > 0 ? planCount : 1L;
            throw new EntityInUseException(
                    "Não é possível excluir uma safra que possui " + effectiveCount + " planos de produção vinculados."
            );
        }

        harvestRepository.deleteById(id);
        log.info("Safra excluída logicamente [harvestId={}] pelo usuário [actorId={}, actorRole={}]",
                id, loggedUser.getId(), loggedUser.getRole());
    }
}
