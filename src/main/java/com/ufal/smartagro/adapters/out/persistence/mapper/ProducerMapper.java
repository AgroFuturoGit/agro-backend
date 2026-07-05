package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ProducerEntity;
import com.ufal.smartagro.domain.model.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProducerMapper {

    private final UserMapper userMapper;
    private final CommunityMapper communityMapper;

    public Producer toDomain(ProducerEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Producer(
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

    public ProducerEntity toEntity(Producer domain) {
        if (domain == null) {
            return null;
        }
        ProducerEntity entity = new ProducerEntity();
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
