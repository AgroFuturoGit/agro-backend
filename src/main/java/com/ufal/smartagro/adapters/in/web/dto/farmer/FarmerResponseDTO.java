package com.ufal.smartagro.adapters.in.web.dto.farmer;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.domain.model.enums.RegistrationSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FarmerResponseDTO(
    UUID id,
    UserResponseDTO user,
    CommunityResponseDTO community,
    String fullName,
    String cpf,
    LocalDate dateOfBirth,
    String motherName,
    String origin,
    String educationLevel,
    String phone,
    String localName,
    String street,
    String city,
    String state,
    String ibgeCode,
    BigDecimal latitude,
    BigDecimal longitude,
    RegistrationSource registrationSource,
    Boolean isCompliant,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public String aliasName() {
        return localName;
    }
}
