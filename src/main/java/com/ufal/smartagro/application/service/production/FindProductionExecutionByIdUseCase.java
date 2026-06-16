package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindProductionExecutionByIdUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;

    @Transactional(readOnly = true)
    public ProductionExecution findById(UUID id) {
        return productionExecutionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));
    }
}
