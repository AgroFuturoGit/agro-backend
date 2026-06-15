package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ManagerEntity;
import com.ufal.smartagro.domain.model.Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManagerMapper {

    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;

    public Manager toDomain(ManagerEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Manager(
                entity.getId(),
                userMapper.toDomain(entity.getUser()),
                organizationMapper.toDomain(entity.getOrganization()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public ManagerEntity toEntity(Manager domain) {
        if (domain == null) {
            return null;
        }
        ManagerEntity entity = new ManagerEntity();
        entity.setId(domain.getId());
        entity.setUser(userMapper.toEntity(domain.getUser()));
        entity.setOrganization(organizationMapper.toEntity(domain.getOrganization()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
