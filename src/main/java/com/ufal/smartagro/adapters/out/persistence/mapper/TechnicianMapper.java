package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicianEntity;
import com.ufal.smartagro.domain.model.Technician;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnicianMapper {

    private final UserMapper userMapper;

    public Technician toDomain(TechnicianEntity entity) {
        if (entity == null) {
            return null;
        }
        return Technician.builder()
                .id(entity.getId())
                .user(userMapper.toDomain(entity.getUser()))
                .registrationType(entity.getRegistrationType())
                .registrationNumber(entity.getRegistrationNumber())
                .specialty(entity.getSpecialty())
                .createdBy(userMapper.toDomain(entity.getCreatedBy()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    public TechnicianEntity toEntity(Technician domain) {
        if (domain == null) {
            return null;
        }
        TechnicianEntity entity = new TechnicianEntity();
        entity.setId(domain.getId());
        entity.setUser(userMapper.toEntity(domain.getUser()));
        entity.setRegistrationType(domain.getRegistrationType());
        entity.setRegistrationNumber(domain.getRegistrationNumber());
        entity.setSpecialty(domain.getSpecialty());
        entity.setCreatedBy(userMapper.toEntity(domain.getCreatedBy()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
