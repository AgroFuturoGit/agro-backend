package com.ufal.smartagro.adapters.out.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeção Spring Data JPA para a consulta agregada de planos e execuções do agricultor.
 */
public interface FarmerPlanSummaryProjection {
    UUID getPlanId();
    UUID getCropId();
    String getCropName();
    String getCropVariety();
    String getHarvestType();
    String getUnit();
    String getStatus();
    String getLocationDescription();
    BigDecimal getPlantedAreaHa();
    BigDecimal getTargetKg();
    BigDecimal getRealizedKg();
    BigDecimal getPendingKg();
    LocalDate getPlantingDate();
    LocalDate getExpectedHarvestStart();
    LocalDate getExpectedHarvestEnd();
    LocalDate getLastExecutionDate();
}
