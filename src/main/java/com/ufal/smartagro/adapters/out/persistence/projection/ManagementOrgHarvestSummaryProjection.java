package com.ufal.smartagro.adapters.out.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeção Spring Data JPA para a view materializada mv_org_harvest_summary.
 */
public interface ManagementOrgHarvestSummaryProjection {
    UUID getOrganizationId();
    String getOrganizationName();
    UUID getCommunityId();
    String getCommunityName();
    UUID getCropId();
    String getCropName();
    String getCropVariety();
    String getUnit();
    UUID getHarvestId();
    Long getPlanCount();
    Long getFarmerCount();
    BigDecimal getPlantedAreaHa();
    BigDecimal getTargetKg();
    BigDecimal getRealizedKg();
    LocalDate getLastExecutionDate();
}
