package com.ufal.smartagro.adapters.in.web.mapper;

import com.ufal.smartagro.adapters.in.web.dto.auth.LoginResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.domain.model.User;

public class Mapper {

    public static UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCpf(),
                user.getRole(),
                user.getDateOfBirth());
    }

    public static LoginResponseDTO toLoginResponseDTO(String token, User user){
        return new LoginResponseDTO(token, toUserResponseDTO(user));
    }
}
