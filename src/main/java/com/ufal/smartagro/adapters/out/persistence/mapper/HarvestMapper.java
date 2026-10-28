package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.HarvestEntity;
import com.ufal.smartagro.domain.model.Harvest;
import org.springframework.stereotype.Component;

@Component
public class HarvestMapper {

    public Harvest toDomain(HarvestEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Harvest(
                entity.getId(),
                entity.getLabel(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public HarvestEntity toEntity(Harvest domain) {
        if (domain == null) {
            return null;
        }

        HarvestEntity harvestEntity = new HarvestEntity();
        harvestEntity.setId(domain.getId());
        harvestEntity.setLabel(domain.getLabel());
        harvestEntity.setStartDate(domain.getStartDate());
        harvestEntity.setEndDate(domain.getEndDate());
        harvestEntity.setCreatedAt(domain.getCreatedAt());
        harvestEntity.setUpdatedAt(domain.getUpdatedAt());
        harvestEntity.setDeletedAt(domain.getDeletedAt());
        return harvestEntity;
    }
}
