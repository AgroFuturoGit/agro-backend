package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class User {
    private UUID id;
    private String fullName;
    private String email;
    private String password;
    private String cpf;
    private Role role;
}
