package com.ufal.smartagro.adapters.in.web.dto.technician;

import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;

public record TechnicianUpdateDTO(
    ProfessionalRegistrationType registrationType,
    String registrationNumber,
    String specialty,
    String professionalId
) {
    public TechnicianUpdateDTO(ProfessionalRegistrationType registrationType, String registrationNumber, String specialty) {
        this(registrationType, registrationNumber, specialty, null);
    }

    public TechnicianUpdateDTO(String professionalId, String specialty) {
        this(parseType(professionalId), parseNumber(professionalId), specialty, professionalId);
    }

    private static ProfessionalRegistrationType parseType(String profId) {
        if (profId == null) return null;
        String[] parts = profId.split("-", 2);
        try {
            return ProfessionalRegistrationType.valueOf(parts[0].trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private static String parseNumber(String profId) {
        if (profId == null) return null;
        String[] parts = profId.split("-", 2);
        if (parts.length > 1) {
            try {
                ProfessionalRegistrationType.valueOf(parts[0].trim().toUpperCase());
                return parts[1].trim();
            } catch (Exception e) {
                return profId;
            }
        }
        return profId;
    }
}
