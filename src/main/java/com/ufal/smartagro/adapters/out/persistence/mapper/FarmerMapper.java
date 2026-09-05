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
        return new Farmer(
                entity.getId(),
                userMapper.toDomain(entity.getUser()),
                communityMapper.toDomain(entity.getCommunity()),
                entity.getAliasName(),
                entity.getIsCompliant(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public FarmerEntity toEntity(Farmer domain) {
        if (domain == null) {
            return null;
        }
        FarmerEntity entity = new FarmerEntity();
        entity.setId(domain.getId());
        entity.setUser(userMapper.toEntity(domain.getUser()));
        entity.setCommunity(communityMapper.toEntity(domain.getCommunity()));
        entity.setAliasName(domain.getAliasName());
        entity.setIsCompliant(domain.getIsCompliant());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
