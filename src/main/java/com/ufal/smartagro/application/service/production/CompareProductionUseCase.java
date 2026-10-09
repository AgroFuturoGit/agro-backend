package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionComparisonDTO;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/** Compara a meta calculada do plano com a soma dos apontamentos validados. */
@Service
@RequiredArgsConstructor
public class CompareProductionUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    /** Calcula desvio e percentual realizado usando área × produtividade e kg validados. */
    public ProductionComparisonDTO compare(UUID planId, User loggedUser) {
        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plano de produção não encontrado."));

        accessValidator.validateAccess(plan.getFarmer(), loggedUser);

        List<ProductionExecution> executions = productionExecutionRepository.findAllByProductionPlanId(planId);

        BigDecimal totalActualYield = executions.stream()
                .filter(e -> e.getStatus() == com.ufal.smartagro.domain.model.enums.ExecutionStatus.VALIDATED)
                .map(ProductionExecution::getQuantityKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedYield = plan.getPlantedArea().multiply(plan.getExpectedProductivity());
        BigDecimal difference = totalActualYield.subtract(expectedYield);

        BigDecimal percentageRealized = BigDecimal.ZERO;
        if (expectedYield.compareTo(BigDecimal.ZERO) > 0) {
            percentageRealized = totalActualYield
                    .multiply(new BigDecimal("100"))
                    .divide(expectedYield, 2, RoundingMode.HALF_UP);
        }

        return new ProductionComparisonDTO(
                plan.getId(),
                expectedYield,
                totalActualYield,
                difference,
                percentageRealized
        );
    }
}
