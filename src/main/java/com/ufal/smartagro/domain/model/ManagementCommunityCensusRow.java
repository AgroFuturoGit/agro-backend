package com.ufal.smartagro.domain.model;

import java.util.UUID;

/**
 * Representa o censo de agricultores ativos e adimplentes por comunidade em uma organização.
 */
public record ManagementCommunityCensusRow(
        UUID organizationId,
        String organizationName,
        UUID communityId,
        String communityName,
        long activeFarmersCount,
        long compliantFarmersCount
) {
}
