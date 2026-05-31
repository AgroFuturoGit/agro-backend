package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.CropEntity;
import com.ufal.smartagro.domain.model.Crop;
import org.springframework.stereotype.Component;

@Component
public class CropMapper {

    public Crop toDomain(CropEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Crop(
                entity.getId(),
                entity.getName(),
                entity.getVariety(),
                entity.getIsPriority()
        );
    }

    public CropEntity toEntity(Crop domain) {
        if (domain == null) {
            return null;
        }

        CropEntity cropEntity = new CropEntity();
        cropEntity.setId(domain.getId());
        cropEntity.setName(domain.getName());
        cropEntity.setVariety(domain.getVariety());
        cropEntity.setIsPriority(domain.getIsPriority());
        return cropEntity;
    }
}
