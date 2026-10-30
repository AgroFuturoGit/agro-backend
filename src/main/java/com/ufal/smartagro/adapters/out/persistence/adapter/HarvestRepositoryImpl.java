package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.HarvestEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.HarvestMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaHarvestRepository;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HarvestRepositoryImpl implements HarvestRepository {

    private final JpaHarvestRepository jpaHarvestRepository;
    private final HarvestMapper harvestMapper;

    @Override
    public Harvest save(Harvest harvest) {
        HarvestEntity entity = harvestMapper.toEntity(harvest);
        HarvestEntity savedEntity = jpaHarvestRepository.save(entity);
        return harvestMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByLabel(String label) {
        return jpaHarvestRepository.existsByLabel(label);
    }

    @Override
    public Optional<Harvest> findById(UUID id) {
        return jpaHarvestRepository.findById(id).map(harvestMapper::toDomain);
    }

    @Override
    public List<Harvest> findAll() {
        return jpaHarvestRepository.findAll().stream()
                .map(cropMapper -> harvestMapper.toDomain(cropMapper))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaHarvestRepository.softDeleteById(id);
    }

    @Override
    public Optional<Harvest> findActiveHarvest(LocalDate date) {
        LocalDate referenceDate = date != null ? date : LocalDate.now();
        return jpaHarvestRepository.findActiveHarvests(referenceDate).stream()
                .findFirst()
                .map(harvestMapper::toDomain);
    }
}
