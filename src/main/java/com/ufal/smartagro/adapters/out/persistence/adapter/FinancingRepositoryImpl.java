package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.FinancingMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaFinancingRepository;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository @RequiredArgsConstructor
public class FinancingRepositoryImpl implements FinancingRepository {
    private final JpaFinancingRepository jpaRepository;
    private final FinancingMapper mapper;
    public Financing save(Financing value) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(value))); }
    public Optional<Financing> findById(UUID id) { return jpaRepository.findById(id).map(mapper::toDomain); }
    public List<Financing> findAllByFarmerId(UUID farmerId) { return jpaRepository.findAllByFarmerId(farmerId).stream().map(mapper::toDomain).toList(); }
    public void delete(UUID id) { jpaRepository.softDeleteById(id); }
}
