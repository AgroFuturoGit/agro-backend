package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductionExecution {
    private UUID id;
    private ProductionPlan productionPlan;
    private BigDecimal actualYield;
    private LocalDate harvestDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
