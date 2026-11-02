package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteProductionPlanUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID planId, User loggedUser) {
        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plano de produção não encontrado."));

        accessValidator.validateWriteAccess(plan.getFarmer(), loggedUser);

        int executionCount = productionExecutionRepository.findAllByProductionPlanId(planId).size();
        if (executionCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir um plano de produção que possui " + executionCount + " apontamentos de colheita vinculados."
            );
        }

        productionPlanRepository.delete(planId);
        log.info("Plano de produção excluído logicamente [planId={}] pelo usuário [actorId={}, actorRole={}]",
                planId, loggedUser.getId(), loggedUser.getRole());
    }
}
