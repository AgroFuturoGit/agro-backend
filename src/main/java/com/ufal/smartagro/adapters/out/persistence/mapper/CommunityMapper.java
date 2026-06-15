package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.CommunityEntity;
import com.ufal.smartagro.domain.model.Community;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityMapper {

    private final OrganizationMapper organizationMapper;

    public Community toDomain(CommunityEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Community(
                entity.getId(),
                entity.getName(),
                organizationMapper.toDomain(entity.getOrganization()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public CommunityEntity toEntity(Community domain) {
        if (domain == null) {
            return null;
        }
        CommunityEntity entity = new CommunityEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setOrganization(organizationMapper.toEntity(domain.getOrganization()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
