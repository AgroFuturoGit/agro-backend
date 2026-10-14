package com.ufal.smartagro.adapters.in.web.dto.technician;

import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record TechnicianRegisterDTO(
    @NotBlank(message = "O nome completo é obrigatório")
    String fullName,

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ser válido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    String password,

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "O CPF deve ser válido")
    String cpf,

    ProfessionalRegistrationType registrationType,
    String registrationNumber,
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
        if (profId == null) return ProfessionalRegistrationType.CREA;
        String[] parts = profId.split("-", 2);
        try {
            return ProfessionalRegistrationType.valueOf(parts[0].trim().toUpperCase());
        } catch (Exception e) {
            return ProfessionalRegistrationType.CREA;
        }
    }

    private static String parseNumber(String profId) {
        if (profId == null) return null;
        String[] parts = profId.split("-", 2);
        return parts.length > 1 ? parts[1].trim() : profId;
    }
}
