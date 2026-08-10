package com.ufal.smartagro.adapters.in.web.mapper;

import com.ufal.smartagro.adapters.in.web.dto.auth.LoginResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianResponseDTO;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;

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

    public static UserResponseDTO toUserResponseDTO(UserEntity entity) {
        if (entity == null) return null;
        return new UserResponseDTO(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getCpf(),
                entity.getRole(),
                entity.getDateOfBirth());
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

    public static OrganizationResponseDTO toOrganizationResponseDTO(Organization organization) {
        if (organization == null) return null;
        return new OrganizationResponseDTO(
                organization.getId(),
                organization.getName(),
                organization.getTaxId(),
                organization.getType(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }

    public static ManagerResponseDTO toManagerResponseDTO(Manager manager) {
        if (manager == null) return null;
        return new ManagerResponseDTO(
                manager.getId(),
                toUserResponseDTO(manager.getUser()),
                toOrganizationResponseDTO(manager.getOrganization()),
                manager.getCreatedAt(),
                manager.getUpdatedAt()
        );
    }

    public static CommunityResponseDTO toCommunityResponseDTO(Community community) {
        if (community == null) return null;
        return new CommunityResponseDTO(
                community.getId(),
                community.getName(),
                toOrganizationResponseDTO(community.getOrganization()),
                community.getCreatedAt(),
                community.getUpdatedAt()
        );
    }

    public static ProducerResponseDTO toProducerResponseDTO(Producer producer) {
        if (producer == null) return null;
        return new ProducerResponseDTO(
                producer.getId(),
                toUserResponseDTO(producer.getUser()),
                toCommunityResponseDTO(producer.getCommunity()),
                producer.getAliasName(),
                producer.getIsCompliant(),
                producer.getCreatedAt(),
                producer.getUpdatedAt()
        );
    }
    public static ProductionPlanResponseDTO toProductionPlanResponseDTO(com.ufal.smartagro.domain.model.ProductionPlan plan) {
        if (plan == null) return null;
        return new ProductionPlanResponseDTO(
                plan.getId(),
                toProducerResponseDTO(plan.getProducer()),
                toHarvestResponseDTO(plan.getHarvest()),
                toCropResponseDTO(plan.getCrop()),
                plan.getPlantedArea(),
                plan.getExpectedYield(),
                plan.getPlannedPlantingDate(),
                plan.getPlannedCalendar(),
                plan.getCreatedAt()
        );
    }

    public static ProductionExecutionResponseDTO toProductionExecutionResponseDTO(com.ufal.smartagro.domain.model.ProductionExecution exec) {
        if (exec == null) return null;
        return new ProductionExecutionResponseDTO(
                exec.getId(),
                exec.getProductionPlan() != null ? exec.getProductionPlan().getId() : null,
                exec.getActualYield(),
                exec.getHarvestDate(),
                exec.getCreatedAt()
        );
    }

    public static TechnicianResponseDTO toTechnicianResponseDTO(Technician technician) {
        if (technician == null) return null;
        return new TechnicianResponseDTO(
                technician.getId(),
                toUserResponseDTO(technician.getUser()),
                technician.getProfessionalId(),
                technician.getSpecialty(),
                technician.getCreatedAt(),
                technician.getUpdatedAt()
        );
    }
}
