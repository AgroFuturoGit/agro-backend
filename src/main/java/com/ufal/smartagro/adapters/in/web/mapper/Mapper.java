package com.ufal.smartagro.adapters.in.web.mapper;

import com.ufal.smartagro.adapters.in.web.dto.auth.LoginResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestUpdateDTO;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Harvest;
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

    public static Crop toCrop(CropUpdateDTO dto) {
        return new Crop(
                null, 
                dto.name(),
                dto.variety(),
                dto.isPriority()
        );
    }

    public static Harvest toHarvest(HarvestRegisterDTO dto) {
        return new Harvest(
                null,
                dto.label(),
                dto.startDate(),
                dto.endDate()
        );
    }

    public static HarvestResponseDTO toHarvestResponseDTO(Harvest harvest) {
        return new HarvestResponseDTO(
                harvest.getId(),
                harvest.getLabel(),
                harvest.getStartDate(),
                harvest.getEndDate()
        );
    }

    public static Harvest toHarvest(HarvestUpdateDTO dto) {
        return new Harvest(
                null,
                dto.label(),
                dto.startDate(),
                dto.endDate()
        );
    }
}
