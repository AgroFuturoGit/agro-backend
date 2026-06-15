package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.CommunityEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.CommunityMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaCommunityRepository;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommunityRepositoryImpl implements CommunityRepository {

    private final JpaCommunityRepository jpaRepository;
    private final CommunityMapper mapper;

    @Override
    public Community save(Community community) {
        CommunityEntity entity = mapper.toEntity(community);
        CommunityEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Community> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
