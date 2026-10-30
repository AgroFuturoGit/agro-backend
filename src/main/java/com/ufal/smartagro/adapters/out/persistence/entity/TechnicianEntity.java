package com.ufal.smartagro.adapters.out.persistence.entity;

import com.ufal.smartagro.domain.model.enums.ProfessionalRegistrationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "technicians")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_at IS NULL")
public class TechnicianEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false)
    private ProfessionalRegistrationType registrationType;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "specialty")
    private String specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Deprecated
    public String getProfessionalId() {
        if (registrationType != null && registrationNumber != null) {
            return registrationType.name() + "-" + registrationNumber;
        }
        return registrationNumber;
    }

    @Deprecated
    public void setProfessionalId(String professionalId) {
        if (professionalId != null) {
            String[] parts = professionalId.split("-", 2);
            if (parts.length == 2) {
                try {
                    this.registrationType = ProfessionalRegistrationType.valueOf(parts[0].trim().toUpperCase());
                    this.registrationNumber = parts[1].trim();
                } catch (Exception e) {
                    this.registrationType = ProfessionalRegistrationType.CREA;
                    this.registrationNumber = professionalId;
                }
            } else {
                this.registrationType = ProfessionalRegistrationType.CREA;
                this.registrationNumber = professionalId;
            }
        }
    }
}
