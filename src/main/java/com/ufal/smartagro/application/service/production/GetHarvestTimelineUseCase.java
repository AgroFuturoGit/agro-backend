package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TimelinePointDTO;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetHarvestTimelineUseCase {

    private static final long MAX_TIMELINE_SPAN_DAYS = 1826; // ~5 anos de proteção contra datas anômalas

    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public HarvestTimelineDTO execute(UUID planId, TimelineGranularity granularity, User loggedUser) {
        if (granularity == null) {
            granularity = TimelineGranularity.WEEK;
        }

        ProductionPlan plan = productionPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plano de produção não encontrado."));

        accessValidator.validateAccess(plan.getFarmer(), loggedUser);

        Crop crop = plan.getCrop();
        HarvestType harvestType = (crop != null && crop.getHarvestType() != null)
                ? crop.getHarvestType()
                : HarvestType.SINGLE;

        List<ProductionExecution> executions = productionExecutionRepository.findAllByProductionPlanId(planId);

        if (harvestType == HarvestType.SINGLE) {
            return buildSingleHarvestTimeline(plan, executions, granularity);
        } else {
            return buildContinuousHarvestTimeline(plan, executions, granularity);
        }
    }

    private HarvestTimelineDTO buildSingleHarvestTimeline(
            ProductionPlan plan,
            List<ProductionExecution> executions,
            TimelineGranularity granularity
    ) {
        List<ProductionExecution> validExecutions = executions.stream()
                .filter(e -> e.getStatus() == ExecutionStatus.VALIDATED || e.getStatus() == ExecutionStatus.PENDING)
                .toList();

        BigDecimal expectedTotal = plan.getExpectedYield() != null ? plan.getExpectedYield() : BigDecimal.ZERO;
        boolean hasExpectedYield = expectedTotal.compareTo(BigDecimal.ZERO) > 0;
        boolean hasDates = plan.getExpectedHarvestStart() != null || plan.getExpectedHarvestEnd() != null;

        if (!hasExpectedYield && !hasDates && validExecutions.isEmpty()) {
            boolean hasExpectedWindow = plan.getExpectedHarvestStart() != null && plan.getExpectedHarvestEnd() != null && !plan.getExpectedHarvestStart().isAfter(plan.getExpectedHarvestEnd());
            return new HarvestTimelineDTO(plan.getId(), HarvestType.SINGLE, granularity, "kg", hasExpectedWindow, List.of());
        }

        BigDecimal actualTotal = BigDecimal.ZERO;
        BigDecimal pendingTotal = BigDecimal.ZERO;

        for (ProductionExecution execution : validExecutions) {
            BigDecimal qty = execution.getQuantityKg() != null ? execution.getQuantityKg() : BigDecimal.ZERO;
            if (execution.getStatus() == ExecutionStatus.VALIDATED) {
                actualTotal = actualTotal.add(qty);
            } else if (execution.getStatus() == ExecutionStatus.PENDING) {
                pendingTotal = pendingTotal.add(qty);
            }
        }

        LocalDate pointDate;
        if (plan.getExpectedHarvestEnd() != null) {
            pointDate = plan.getExpectedHarvestEnd();
        } else if (plan.getExpectedHarvestStart() != null) {
            pointDate = plan.getExpectedHarvestStart();
        } else if (!validExecutions.isEmpty()) {
            pointDate = validExecutions.stream()
                    .map(ProductionExecution::getHarvestedAt)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(LocalDate.now());
        } else if (plan.getPlannedPlantingDate() != null) {
            pointDate = plan.getPlannedPlantingDate();
        } else {
            pointDate = LocalDate.now();
        }

        TimelinePointDTO point = new TimelinePointDTO(
                pointDate,
                expectedTotal.setScale(2, RoundingMode.HALF_UP),
                actualTotal.setScale(2, RoundingMode.HALF_UP),
                pendingTotal.setScale(2, RoundingMode.HALF_UP)
        );

        boolean hasExpectedWindow = plan.getExpectedHarvestStart() != null && plan.getExpectedHarvestEnd() != null && !plan.getExpectedHarvestStart().isAfter(plan.getExpectedHarvestEnd());
        return new HarvestTimelineDTO(
                plan.getId(),
                HarvestType.SINGLE,
                granularity,
                "kg",
                hasExpectedWindow,
                List.of(point)
        );
    }

    private HarvestTimelineDTO buildContinuousHarvestTimeline(
            ProductionPlan plan,
            List<ProductionExecution> executions,
            TimelineGranularity granularity
    ) {
        List<ProductionExecution> validExecutions = executions.stream()
                .filter(e -> (e.getStatus() == ExecutionStatus.VALIDATED || e.getStatus() == ExecutionStatus.PENDING)
                        && e.getHarvestedAt() != null)
                .toList();

        LocalDate planStart = plan.getExpectedHarvestStart();
        LocalDate planEnd = plan.getExpectedHarvestEnd();
        boolean hasValidPlanWindow = planStart != null && planEnd != null && !planStart.isAfter(planEnd);

        if (!hasValidPlanWindow && validExecutions.isEmpty()) {
            return new HarvestTimelineDTO(plan.getId(), HarvestType.CONTINUOUS, granularity, "kg", hasValidPlanWindow, List.of());
        }

        LocalDate minDate = null;
        LocalDate maxDate = null;

        if (hasValidPlanWindow) {
            minDate = planStart;
            maxDate = planEnd;
        }

        for (ProductionExecution e : validExecutions) {
            LocalDate hDate = e.getHarvestedAt();
            if (minDate == null || hDate.isBefore(minDate)) {
                minDate = hDate;
            }
            if (maxDate == null || hDate.isAfter(maxDate)) {
                maxDate = hDate;
            }
        }

        if (ChronoUnit.DAYS.between(minDate, maxDate) > MAX_TIMELINE_SPAN_DAYS) {
            maxDate = minDate.plusDays(MAX_TIMELINE_SPAN_DAYS);
        }

        BigDecimal totalExpectedMeta = hasValidPlanWindow && plan.getExpectedYield() != null
                ? plan.getExpectedYield()
                : BigDecimal.ZERO;

        long totalWindowDays = hasValidPlanWindow
                ? ChronoUnit.DAYS.between(planStart, planEnd) + 1
                : 0;

        List<TimelinePointDTO> points = new ArrayList<>();
        LocalDate current = (granularity == TimelineGranularity.MONTH)
                ? minDate.withDayOfMonth(1)
                : minDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        long cumulativeOverlapDays = 0;
        BigDecimal previousCumulativeExpected = BigDecimal.ZERO;

        while (!current.isAfter(maxDate)) {
            LocalDate bucketStart = current;
            LocalDate bucketEnd;
            LocalDate nextBucketStart;

            if (granularity == TimelineGranularity.MONTH) {
                bucketEnd = current.with(TemporalAdjusters.lastDayOfMonth());
                nextBucketStart = current.plusMonths(1);
            } else {
                bucketEnd = current.plusDays(6);
                nextBucketStart = current.plusDays(7);
            }

            BigDecimal actualSum = BigDecimal.ZERO;
            BigDecimal pendingSum = BigDecimal.ZERO;

            for (ProductionExecution e : validExecutions) {
                LocalDate hDate = e.getHarvestedAt();
                if (!hDate.isBefore(bucketStart) && !hDate.isAfter(bucketEnd)) {
                    BigDecimal qty = e.getQuantityKg() != null ? e.getQuantityKg() : BigDecimal.ZERO;
                    if (e.getStatus() == ExecutionStatus.VALIDATED) {
                        actualSum = actualSum.add(qty);
                    } else if (e.getStatus() == ExecutionStatus.PENDING) {
                        pendingSum = pendingSum.add(qty);
                    }
                }
            }

            BigDecimal expectedValue;
            if (hasValidPlanWindow && totalWindowDays > 0) {
                LocalDate overlapStart = bucketStart.isBefore(planStart) ? planStart : bucketStart;
                LocalDate overlapEnd = bucketEnd.isAfter(planEnd) ? planEnd : bucketEnd;

                long overlapDays = 0;
                if (!overlapStart.isAfter(overlapEnd)) {
                    overlapDays = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
                }
                cumulativeOverlapDays += overlapDays;

                BigDecimal currentCumulativeExpected = totalExpectedMeta
                        .multiply(BigDecimal.valueOf(cumulativeOverlapDays))
                        .divide(BigDecimal.valueOf(totalWindowDays), 2, RoundingMode.HALF_UP);

                expectedValue = currentCumulativeExpected.subtract(previousCumulativeExpected);
                previousCumulativeExpected = currentCumulativeExpected;
            } else {
                expectedValue = BigDecimal.ZERO;
            }

            points.add(new TimelinePointDTO(
                    bucketStart,
                    expectedValue.setScale(2, RoundingMode.HALF_UP),
                    actualSum.setScale(2, RoundingMode.HALF_UP),
                    pendingSum.setScale(2, RoundingMode.HALF_UP)
            ));

            current = nextBucketStart;
        }

        return new HarvestTimelineDTO(
                plan.getId(),
                HarvestType.CONTINUOUS,
                granularity,
                "kg",
                hasValidPlanWindow,
                points
        );
    }
}
