package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.TechnicianEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.TechnicianMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaTechnicianRepository;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TechnicianRepositoryImpl implements TechnicianRepository {

    private final JpaTechnicianRepository jpaRepository;
    private final TechnicianMapper mapper;

    @Override
    public Technician save(Technician technician) {
        TechnicianEntity entity = mapper.toEntity(technician);
        TechnicianEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Technician> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Technician> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<Technician> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.softDeleteById(id);
    }
}
