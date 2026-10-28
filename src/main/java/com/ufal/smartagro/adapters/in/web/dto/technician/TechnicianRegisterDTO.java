package com.ufal.smartagro.adapters.in.web.dto.technician;

import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record TechnicianRegisterDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        @Size(max = 150, message = "O nome completo deve ter no máximo 150 caracteres")
        String fullName,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
        String password,

        @NotBlank(message = "O CPF é obrigatório")
        @CPF(message = "O CPF deve ser válido")
        String cpf,

        ProfessionalRegistrationType registrationType,

        @Size(max = 50, message = "O número de registro deve ter no máximo 50 caracteres")
        String registrationNumber,

        @Size(max = 100, message = "A especialidade deve ter no máximo 100 caracteres")
        String specialty
) {
    public TechnicianRegisterDTO(
            String fullName,
            String email,
            String password,
            String cpf,
            LocalDate dateOfBirth,
            String professionalId,
            String specialty
    ) {
        this(fullName, email, password, cpf, parseType(professionalId), parseNumber(professionalId), specialty);
    }

    public String professionalId() {
        if (registrationType != null && registrationNumber != null) {
            return registrationType.name() + "-" + registrationNumber;
        }
        return registrationNumber;
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
