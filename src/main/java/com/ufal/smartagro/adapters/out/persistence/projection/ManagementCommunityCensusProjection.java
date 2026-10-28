package com.ufal.smartagro.adapters.out.persistence.projection;

import java.util.UUID;

/**
 * Projeção Spring Data JPA para o censo de comunidades e agricultores do dashboard gerencial.
 */
public interface ManagementCommunityCensusProjection {
    UUID getOrganizationId();
    String getOrganizationName();
    UUID getCommunityId();
    String getCommunityName();
    Long getActiveFarmersCount();
    Long getCompliantFarmersCount();
}
