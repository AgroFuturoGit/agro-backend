package com.ufal.smartagro.adapters.in.web.dto.user;

import com.ufal.smartagro.domain.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UserRegisterDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        String fullName,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password,

        @CPF(message = "O CPF deve ser válido")
        String cpf,

        @NotNull(message = "A role é obrigatória")
        Role role
) {
}
