package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID executionId, User loggedUser) {
        ProductionExecution execution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Execução de produção não encontrada."));

        if (execution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(execution.getProductionPlan().getFarmer(), loggedUser);
        }

        productionExecutionRepository.delete(executionId);
        log.info("Apontamento de produção excluído logicamente [executionId={}] pelo usuário [actorId={}, actorRole={}]",
                executionId, loggedUser.getId(), loggedUser.getRole());
    }
}
