package com.ufal.smartagro.adapters.in.web.dto.farmer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;
import java.time.LocalDate;

public record FarmerRegisterDTO(
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
    @NotNull(message = "A data de nascimento é obrigatória")
    LocalDate dateOfBirth,
    String aliasName
) {}
