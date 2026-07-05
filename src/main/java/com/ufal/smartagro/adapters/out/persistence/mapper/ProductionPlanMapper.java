package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionPlanEntity;
import com.ufal.smartagro.domain.model.ProductionPlan;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductionPlanMapper {

    private final ProducerMapper producerMapper;
    private final HarvestMapper harvestMapper;
    private final CropMapper cropMapper;

    public ProductionPlan toDomain(ProductionPlanEntity entity) {
        if (entity == null) return null;
        return new ProductionPlan(
                entity.getId(),
                producerMapper.toDomain(entity.getProducer()),
                harvestMapper.toDomain(entity.getHarvest()),
                cropMapper.toDomain(entity.getCrop()),
                entity.getPlantedArea(),
                entity.getExpectedYield(),
                entity.getPlannedPlantingDate(),
                entity.getPlannedCalendar(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public ProductionPlanEntity toEntity(ProductionPlan domain) {
        if (domain == null) return null;
        ProductionPlanEntity entity = new ProductionPlanEntity();
        entity.setId(domain.getId());
        entity.setProducer(producerMapper.toEntity(domain.getProducer()));
        entity.setHarvest(harvestMapper.toEntity(domain.getHarvest()));
        entity.setCrop(cropMapper.toEntity(domain.getCrop()));
        entity.setPlantedArea(domain.getPlantedArea());
        entity.setExpectedYield(domain.getExpectedYield());
        entity.setPlannedPlantingDate(domain.getPlannedPlantingDate());
        entity.setPlannedCalendar(domain.getPlannedCalendar());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
