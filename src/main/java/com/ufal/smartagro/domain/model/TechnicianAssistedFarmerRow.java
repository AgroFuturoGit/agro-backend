package com.ufal.smartagro.domain.model;

import java.util.UUID;

/**
 * Registro da carteira de assistência técnica (comunidade e agricultor assistido).
 */
public record TechnicianAssistedFarmerRow(
        UUID communityId,
        String communityName,
        UUID farmerId,
        String farmerName,
        Boolean isCompliant
) {
}
