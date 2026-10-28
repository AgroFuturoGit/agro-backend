package com.ufal.smartagro.adapters.in.web.dto.user;

import com.ufal.smartagro.domain.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UserRegisterDTO(
        @NotBlank(message = "O nome completo é obrigatório")
        @Size(min = 2, max = 150, message = "O nome completo deve ter entre 2 e 150 caracteres")
        String fullName,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
        String password,

        @CPF(message = "O CPF deve ser válido")
        String cpf,

        @NotNull(message = "A role é obrigatória")
        Role role
) {
}
