package com.ufal.smartagro.adapters.in.web.dto.farmer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FarmerRegisterDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        @Size(max = 150, message = "O nome completo deve ter no máximo 150 caracteres")
        String fullName,

        @NotBlank(message = "O CPF é obrigatório")
        @CPF(message = "O CPF deve ser válido")
        String cpf,

        @Email(message = "O email deve ser válido")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,

        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
        String password,

        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dateOfBirth,

        @Size(max = 150, message = "O nome da mãe deve ter no máximo 150 caracteres")
        String motherName,

        @Size(max = 100, message = "A origem deve ter no máximo 100 caracteres")
        String origin,

        @Size(max = 100, message = "O nível de escolaridade deve ter no máximo 100 caracteres")
        String educationLevel,

        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String phone,

        @Size(max = 100, message = "O apelido/nome local deve ter no máximo 100 caracteres")
        String localName,

        @Size(max = 100, message = "A rua deve ter no máximo 100 caracteres")
        String street,

        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
        String city,

        @Size(max = 50, message = "O estado deve ter no máximo 50 caracteres")
        String state,

        @Size(max = 10, message = "O código IBGE deve ter no máximo 10 caracteres")
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
