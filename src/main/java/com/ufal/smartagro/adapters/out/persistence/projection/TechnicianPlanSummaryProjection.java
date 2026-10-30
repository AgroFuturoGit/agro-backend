package com.ufal.smartagro.adapters.out.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeção Spring Data JPA para os planos agregados da carteira do técnico.
 */
public interface TechnicianPlanSummaryProjection {
    UUID getPlanId();
    UUID getFarmerId();
    String getFarmerName();
    UUID getCommunityId();
    String getCommunityName();
    UUID getCropId();
    String getCropName();
    String getCropVariety();
    String getHarvestType();
    String getUnit();
    String getStatus();
    BigDecimal getPlantedAreaHa();
    BigDecimal getTargetKg();
    BigDecimal getRealizedKg();
    BigDecimal getPendingKg();
    Long getPendingExecutionsCount();
    LocalDate getPlantingDate();
    LocalDate getExpectedHarvestStart();
    LocalDate getExpectedHarvestEnd();
    LocalDate getLastExecutionDate();
}
