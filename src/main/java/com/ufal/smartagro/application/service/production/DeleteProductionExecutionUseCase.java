package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;

    @Transactional
    public void delete(UUID executionId) {
        productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));
        
        productionExecutionRepository.delete(executionId);
    }
}
