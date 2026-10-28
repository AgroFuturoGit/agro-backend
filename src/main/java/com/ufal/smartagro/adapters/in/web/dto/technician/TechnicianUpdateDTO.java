package com.ufal.smartagro.adapters.in.web.dto.technician;

import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
import jakarta.validation.constraints.Size;

public record TechnicianUpdateDTO(
        ProfessionalRegistrationType registrationType,

        @Size(max = 50, message = "O número de registro deve ter no máximo 50 caracteres")
        String registrationNumber,

        @Size(max = 100, message = "A especialidade deve ter no máximo 100 caracteres")
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
