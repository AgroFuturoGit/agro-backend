package com.ufal.smartagro.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import com.ufal.smartagro.domain.model.enums.PlanStatus;

/** Plano agrícola com produtividade congelada para manter estável a meta planejada. */
@Getter
public class ProductionPlan {
    private UUID id;
    /** Identificador de idempotência gerado no cliente offline. */
    private UUID clientId;
    private Farmer farmer;
    private Harvest harvest;
    private Crop crop;
    private BigDecimal plantedArea;
    private PlanStatus status;
    private LocalDate expectedHarvestStart;
    private LocalDate expectedHarvestEnd;
    /** Produtividade copiada da cultura quando o plano foi criado. */
    private BigDecimal expectedProductivity;
    /** Texto e coordenadas do local onde a produção foi planejada. */
    private String locationDescription;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate plannedPlantingDate;
    private Map<String, Object> plannedCalendar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public ProductionPlan(UUID id, UUID clientId, Farmer farmer, Harvest harvest, Crop crop, BigDecimal plantedArea,
                          PlanStatus status, LocalDate expectedHarvestStart, LocalDate expectedHarvestEnd,
                          BigDecimal expectedProductivity, String locationDescription, BigDecimal latitude,
                          BigDecimal longitude, LocalDate plannedPlantingDate, Map<String, Object> plannedCalendar,
                          LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.clientId = clientId;
        this.farmer = farmer;
        this.harvest = harvest;
        this.crop = crop;
        this.plantedArea = plantedArea;
        this.status = status;
        this.expectedHarvestStart = expectedHarvestStart;
        this.expectedHarvestEnd = expectedHarvestEnd;
        this.expectedProductivity = expectedProductivity;
        this.locationDescription = locationDescription;
        this.latitude = latitude;
        this.longitude = longitude;
        this.plannedPlantingDate = plannedPlantingDate;
        this.plannedCalendar = plannedCalendar;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /** Construtor de compatibilidade para chamadas sem clientId. */
    public ProductionPlan(UUID id, Farmer farmer, Harvest harvest, Crop crop, BigDecimal plantedArea,
                          PlanStatus status, LocalDate expectedHarvestStart, LocalDate expectedHarvestEnd,
                          BigDecimal expectedProductivity, String locationDescription, BigDecimal latitude,
                          BigDecimal longitude, LocalDate plannedPlantingDate, Map<String, Object> plannedCalendar,
                          LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this(id, null, farmer, harvest, crop, plantedArea, status, expectedHarvestStart, expectedHarvestEnd,
                expectedProductivity, locationDescription, latitude, longitude, plannedPlantingDate,
                plannedCalendar, createdAt, updatedAt, deletedAt);
    }

    /** Construtor legado: converte a antiga meta total em produtividade por área. */
    public ProductionPlan(UUID id, Farmer farmer, Harvest harvest, Crop crop, BigDecimal plantedArea,
                          BigDecimal expectedYield, LocalDate plannedPlantingDate, Map<String, Object> plannedCalendar,
                          LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this(id, null, farmer, harvest, crop, plantedArea, com.ufal.smartagro.domain.model.enums.PlanStatus.PLANNED,
                null, null, plantedArea == null || plantedArea.signum() == 0 ? BigDecimal.ZERO : expectedYield.divide(plantedArea, 4, java.math.RoundingMode.HALF_UP),
                null, null, null, plannedPlantingDate, plannedCalendar, createdAt, updatedAt, deletedAt);
    }

    /** Calcula a meta total sem armazenar expectedYield como dado independente. */
    public BigDecimal getExpectedYield() {
        return plantedArea != null && expectedProductivity != null
                ? plantedArea.multiply(expectedProductivity) : BigDecimal.ZERO;
    }
}
