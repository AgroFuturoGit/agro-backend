package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FarmerEntity;
import com.ufal.smartagro.domain.model.Farmer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FarmerMapper {

    private final UserMapper userMapper;
    private final CommunityMapper communityMapper;

    public Farmer toDomain(FarmerEntity entity) {
        if (entity == null) {
            return null;
        }
        return Farmer.builder()
                .id(entity.getId())
                .user(entity.getUser() != null ? userMapper.toDomain(entity.getUser()) : null)
                .community(entity.getCommunity() != null ? communityMapper.toDomain(entity.getCommunity()) : null)
                .fullName(entity.getFullName())
                .cpf(entity.getCpf())
                .dateOfBirth(entity.getDateOfBirth())
                .motherName(entity.getMotherName())
                .origin(entity.getOrigin())
                .educationLevel(entity.getEducationLevel())
                .phone(entity.getPhone())
                .localName(entity.getLocalName())
                .street(entity.getStreet())
                .city(entity.getCity())
                .state(entity.getState())
                .ibgeCode(entity.getIbgeCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .registrationSource(entity.getRegistrationSource())
                .createdBy(entity.getCreatedBy() != null ? userMapper.toDomain(entity.getCreatedBy()) : null)
                .isCompliant(entity.getIsCompliant())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    public FarmerEntity toEntity(Farmer domain) {
        if (domain == null) {
            return null;
        }
        FarmerEntity entity = new FarmerEntity();
        entity.setId(domain.getId());
        entity.setUser(domain.getUser() != null ? userMapper.toEntity(domain.getUser()) : null);
        entity.setCommunity(domain.getCommunity() != null ? communityMapper.toEntity(domain.getCommunity()) : null);
        entity.setFullName(domain.getFullName());
        entity.setCpf(domain.getCpf());
        entity.setDateOfBirth(domain.getDateOfBirth());
        entity.setMotherName(domain.getMotherName());
        entity.setOrigin(domain.getOrigin());
        entity.setEducationLevel(domain.getEducationLevel());
        entity.setPhone(domain.getPhone());
        entity.setLocalName(domain.getLocalName());
        entity.setStreet(domain.getStreet());
        entity.setCity(domain.getCity());
        entity.setState(domain.getState());
        entity.setIbgeCode(domain.getIbgeCode());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setRegistrationSource(domain.getRegistrationSource());
        entity.setCreatedBy(domain.getCreatedBy() != null ? userMapper.toEntity(domain.getCreatedBy()) : null);
        entity.setIsCompliant(domain.getIsCompliant());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
