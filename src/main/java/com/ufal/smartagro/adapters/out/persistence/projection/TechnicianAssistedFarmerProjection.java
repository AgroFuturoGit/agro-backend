package com.ufal.smartagro.adapters.out.persistence.projection;

import java.util.UUID;

/**
 * Projeção Spring Data JPA para a carteira de assistência do técnico (comunidades e agricultores).
 */
public interface TechnicianAssistedFarmerProjection {
    UUID getCommunityId();
    String getCommunityName();
    UUID getFarmerId();
    String getFarmerName();
    Boolean getIsCompliant();
}
