package com.ufal.smartagro.adapters.in.web.mapper;

import com.ufal.smartagro.adapters.in.web.dto.auth.LoginResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;

public class Mapper {

    public static UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCpf(),
                user.getRole(),
                user.getDateOfBirth());
    }

    public static LoginResponseDTO toLoginResponseDTO(String token, User user){
        return new LoginResponseDTO(token, toUserResponseDTO(user));
    }

    public static User toUser(UserRegisterDTO dto, String encodedPassword) {
        return new User(
                null,
                dto.fullName(),
                dto.email(),
                encodedPassword,
                dto.cpf(),
                dto.dateOfBirth(),
                dto.role()
        );
    }

    public static CropResponseDTO toCropResponseDTO(Crop crop) {
        return new CropResponseDTO(
                crop.getId(),
                crop.getName(),
                crop.getVariety(),
                crop.getIsPriority()
        );
    }

    public static Crop toCrop(CropRegisterDTO dto) {
        return new Crop(
                null,
                dto.name(),
                dto.variety(),
                dto.isPriority()
        );
    }
}
