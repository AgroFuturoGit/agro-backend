package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FinancingEntity;
import com.ufal.smartagro.domain.model.Financing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class FinancingMapper {
    private final FarmerMapper farmerMapper;
    public Financing toDomain(FinancingEntity entity) {
        if (entity == null) return null;
        return Financing.builder().id(entity.getId()).farmer(farmerMapper.toDomain(entity.getFarmer()))
                .description(entity.getDescription()).status(entity.getStatus()).amount(entity.getAmount())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).deletedAt(entity.getDeletedAt()).build();
    }
    public FinancingEntity toEntity(Financing domain) {
        if (domain == null) return null;
        FinancingEntity entity = new FinancingEntity();
        entity.setId(domain.getId()); entity.setFarmer(farmerMapper.toEntity(domain.getFarmer()));
        entity.setDescription(domain.getDescription()); entity.setStatus(domain.getStatus()); entity.setAmount(domain.getAmount());
        entity.setCreatedAt(domain.getCreatedAt()); entity.setUpdatedAt(domain.getUpdatedAt()); entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
