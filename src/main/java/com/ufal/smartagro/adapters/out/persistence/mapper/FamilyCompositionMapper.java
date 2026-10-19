package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FamilyCompositionEntity;
import com.ufal.smartagro.domain.model.FamilyComposition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class FamilyCompositionMapper {
    private final FarmerMapper farmerMapper;
    public FamilyComposition toDomain(FamilyCompositionEntity entity) {
        if (entity == null) return null;
        return FamilyComposition.builder().id(entity.getId()).farmer(farmerMapper.toDomain(entity.getFarmer()))
                .name(entity.getName()).kinship(entity.getKinship()).age(entity.getAge())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).deletedAt(entity.getDeletedAt()).build();
    }
    public FamilyCompositionEntity toEntity(FamilyComposition domain) {
        if (domain == null) return null;
        FamilyCompositionEntity entity = new FamilyCompositionEntity();
        entity.setId(domain.getId()); entity.setFarmer(farmerMapper.toEntity(domain.getFarmer()));
        entity.setName(domain.getName()); entity.setKinship(domain.getKinship()); entity.setAge(domain.getAge());
        entity.setCreatedAt(domain.getCreatedAt()); entity.setUpdatedAt(domain.getUpdatedAt()); entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
