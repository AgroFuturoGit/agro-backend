package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import com.ufal.smartagro.domain.model.ProductionExecution;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductionExecutionMapper {

    private final ProductionPlanMapper productionPlanMapper;

    public ProductionExecution toDomain(ProductionExecutionEntity entity) {
        if (entity == null) return null;
        return new ProductionExecution(
                entity.getId(),
                productionPlanMapper.toDomain(entity.getProductionPlan()),
                entity.getActualYield(),
                entity.getHarvestDate(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public ProductionExecutionEntity toEntity(ProductionExecution domain) {
        if (domain == null) return null;
        ProductionExecutionEntity entity = new ProductionExecutionEntity();
        entity.setId(domain.getId());
        entity.setProductionPlan(productionPlanMapper.toEntity(domain.getProductionPlan()));
        entity.setActualYield(domain.getActualYield());
        entity.setHarvestDate(domain.getHarvestDate());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
