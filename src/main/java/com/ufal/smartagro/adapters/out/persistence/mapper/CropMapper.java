package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.CropEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
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

    public CropEntity toEntity(Crop entity) {
        if (entity == null) {
            return null;
        }

        CropEntity cropEntity = new CropEntity();
        cropEntity.getId();
        cropEntity.getName();
        cropEntity.getVariety();
        cropEntity.getIsPriority();
        return cropEntity;
    }
}
