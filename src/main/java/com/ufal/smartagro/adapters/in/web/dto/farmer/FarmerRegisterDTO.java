package com.ufal.smartagro.adapters.in.web.dto.farmer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FarmerRegisterDTO(
    @NotBlank(message = "O nome completo é obrigatório")
    String fullName,

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "O CPF deve ser válido")
    String cpf,

    @Email(message = "O email deve ser válido")
    String email,

    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    String password,

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
    BigDecimal longitude
) {
    public FarmerRegisterDTO(
            String fullName,
            String email,
            String password,
            String cpf,
            LocalDate dateOfBirth,
            String aliasName
    ) {
        this(fullName, cpf, email, password, dateOfBirth, null, null, null, null, aliasName, null, null, null, null, null, null);
    }

    public String aliasName() {
        return localName;
    }
}
