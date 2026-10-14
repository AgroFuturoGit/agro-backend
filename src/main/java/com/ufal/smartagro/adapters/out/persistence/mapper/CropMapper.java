package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.CropEntity;
import com.ufal.smartagro.domain.model.Crop;
import org.springframework.stereotype.Component;

/** Converte a cultura entre o modelo de domínio e sua representação JPA. */
@Component
public class CropMapper {

    /** Reconstrói a cultura com os parâmetros de produtividade e unidade persistidos. */
    public Crop toDomain(CropEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Crop(
                entity.getId(),
                entity.getName(),
                entity.getVariety(),
                entity.getIsPriority(), entity.getCycleDays(), entity.getExpectedProductivity(), entity.getHarvestType(), entity.getUnit(), entity.getUnitWeightKg()
        );
    }

    /** Prepara a entidade de persistência incluindo todos os novos dados agrícolas. */
    public CropEntity toEntity(Crop domain) {
        if (domain == null) {
            return null;
        }

        CropEntity cropEntity = new CropEntity();
        cropEntity.setId(domain.getId());
        cropEntity.setName(domain.getName());
        cropEntity.setVariety(domain.getVariety());
        cropEntity.setIsPriority(domain.getIsPriority());
        cropEntity.setCycleDays(domain.getCycleDays());
        cropEntity.setExpectedProductivity(domain.getExpectedProductivity());
        cropEntity.setHarvestType(domain.getHarvestType());
        cropEntity.setUnit(domain.getUnit());
        cropEntity.setUnitWeightKg(domain.getUnitWeightKg());
        return cropEntity;
    }
}
