package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.CropEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.CropMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaCropRepository;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.port.out.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CropRepositoryImpl implements CropRepository {

    private final JpaCropRepository jpaCropRepository;
    private final CropMapper cropMapper;

    @Override
    public Crop save(Crop crop) {
        CropEntity entity = cropMapper.toEntity(crop);
        CropEntity savedEntity = jpaCropRepository.save(entity);
        return cropMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByNameAndVariety(String name, String variety){
        return jpaCropRepository.existsByNameAndVariety(name, variety);
    }

    @Override
    public Optional<Crop> findById(UUID id) {
        return jpaCropRepository.findById(id).map(cropMapper::toDomain);
    }

    @Override
    public List<Crop> findAll() {
        return jpaCropRepository.findAll().stream()
                .map(cropMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaCropRepository.softDeleteById(id);
    }
}
