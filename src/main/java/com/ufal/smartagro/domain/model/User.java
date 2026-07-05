package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class User {
    private UUID id;
    private String fullName;
    private String email;
    private String password;
    private String cpf;
    private LocalDate dateOfBirth;
    private Role role;
}
