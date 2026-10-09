package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Technician {
    private UUID id;
    private User user;
    private ProfessionalRegistrationType registrationType;
    private String registrationNumber;
    private String specialty;
    private User createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Technician(UUID id, User user, String professionalId, String specialty, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.user = user;
        this.specialty = specialty;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        if (professionalId != null) {
            String[] parts = professionalId.split("-", 2);
            if (parts.length == 2) {
                try {
                    this.registrationType = ProfessionalRegistrationType.valueOf(parts[0].trim().toUpperCase());
                    this.registrationNumber = parts[1].trim();
                } catch (IllegalArgumentException e) {
                    this.registrationNumber = professionalId;
                }
            } else {
                this.registrationNumber = professionalId;
            }
        }
    }

    public String getProfessionalId() {
        if (registrationType != null && registrationNumber != null) {
            return registrationType.name() + "-" + registrationNumber;
        }
        return registrationNumber;
    }
}
