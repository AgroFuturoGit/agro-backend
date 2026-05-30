package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.CropEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.CropMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaCropRepository;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.port.out.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
