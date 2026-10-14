package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.RegistrationSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {
    private UUID id;
    private User user;
    private Community community;
    private String fullName;
    private String cpf;
    private LocalDate dateOfBirth;
    private String motherName;
    private String origin;
    private String educationLevel;
    private String phone;
    private String localName;
    private String street;
    private String city;
    private String state;
    private String ibgeCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private RegistrationSource registrationSource;
    private User createdBy;
    private Boolean isCompliant;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /**
     * Construtor de compatibilidade para código/testes legados.
     */
    public Farmer(UUID id, User user, Community community, String aliasName, Boolean isCompliant, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.user = user;
        this.community = community;
        this.fullName = user != null ? user.getFullName() : null;
        this.cpf = user != null ? user.getCpf() : null;
        this.localName = aliasName;
        this.isCompliant = isCompliant;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * @deprecated Use {@link #getLocalName()} instead (renomeado conforme V2.0).
     */
    @Deprecated
    public String getAliasName() {
        return localName;
    }
}
