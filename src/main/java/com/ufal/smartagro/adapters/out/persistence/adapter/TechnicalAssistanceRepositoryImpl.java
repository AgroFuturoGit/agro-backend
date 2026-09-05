package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicalAssistanceEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.TechnicalAssistanceMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaTechnicalAssistanceRepository;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TechnicalAssistanceRepositoryImpl implements TechnicalAssistanceRepository {

    private final JpaTechnicalAssistanceRepository jpaRepository;
    private final TechnicalAssistanceMapper mapper;

    @Override
    public TechnicalAssistance save(TechnicalAssistance assistance) {
        TechnicalAssistanceEntity entity = mapper.toEntity(assistance);
        TechnicalAssistanceEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<TechnicalAssistance> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<TechnicalAssistance> findByTechnicianId(UUID technicianId) {
        return jpaRepository.findByTechnicianId(technicianId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TechnicalAssistance> findByFarmerId(UUID farmerId) {
        return jpaRepository.findByFarmerId(farmerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TechnicalAssistance> findActiveByTechnicianAndFarmer(UUID technicianId, UUID farmerId) {
        return jpaRepository.findByTechnicianIdAndFarmerIdAndEndDateIsNull(technicianId, farmerId)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.softDeleteById(id);
    }
}
