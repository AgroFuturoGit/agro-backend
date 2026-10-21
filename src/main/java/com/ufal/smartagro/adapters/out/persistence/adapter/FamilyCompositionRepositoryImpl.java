package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.FamilyCompositionMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaFamilyCompositionRepository;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository @RequiredArgsConstructor
public class FamilyCompositionRepositoryImpl implements FamilyCompositionRepository {
    private final JpaFamilyCompositionRepository jpaRepository;
    private final FamilyCompositionMapper mapper;
    public FamilyComposition save(FamilyComposition value) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(value))); }
    public Optional<FamilyComposition> findById(UUID id) { return jpaRepository.findById(id).map(mapper::toDomain); }
    public List<FamilyComposition> findAllByFarmerId(UUID farmerId) { return jpaRepository.findAllByFarmerId(farmerId).stream().map(mapper::toDomain).toList(); }
    public void delete(UUID id) { jpaRepository.softDeleteById(id); }
}
