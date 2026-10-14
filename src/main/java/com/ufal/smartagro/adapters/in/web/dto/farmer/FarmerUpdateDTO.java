package com.ufal.smartagro.adapters.in.web.dto.farmer;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FarmerUpdateDTO(
    String fullName,
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
    Boolean isCompliant
) {
    public FarmerUpdateDTO(String aliasName) {
        this(null, null, null, null, null, null, aliasName, null, null, null, null, null, null, null);
    }

    public String aliasName() {
        return localName;
    }
}
