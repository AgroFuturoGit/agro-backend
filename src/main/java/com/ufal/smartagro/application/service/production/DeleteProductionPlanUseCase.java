package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
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
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID planId, User loggedUser) {
        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Plano de produção não encontrado."));

        accessValidator.validateWriteAccess(plan.getFarmer(), loggedUser);

        productionPlanRepository.delete(planId);
        log.info("Plano de produção excluído logicamente [planId={}] pelo usuário [actorId={}, actorRole={}]",
                planId, loggedUser.getId(), loggedUser.getRole());
    }
}
