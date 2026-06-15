package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.OrganizationEntity;
import com.ufal.smartagro.domain.model.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toDomain(OrganizationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Organization(
                entity.getId(),
                entity.getName(),
                entity.getTaxId(),
                entity.getType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public OrganizationEntity toEntity(Organization domain) {
        if (domain == null) {
            return null;
        }
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setTaxId(domain.getTaxId());
        entity.setType(domain.getType());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
