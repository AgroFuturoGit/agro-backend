package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ProductionPlan {
    private UUID id;
    private Farmer farmer;
    private Harvest harvest;
    private Crop crop;
    private BigDecimal plantedArea;
    private BigDecimal expectedYield;
    private LocalDate plannedPlantingDate;
    private Map<String, Object> plannedCalendar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
