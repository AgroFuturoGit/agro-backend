package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import com.ufal.smartagro.domain.model.ProductionExecution;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/** Converte apontamentos de colheita e seus dados de validação para/da persistência. */
@Component
@RequiredArgsConstructor
public class ProductionExecutionMapper {

    private final ProductionPlanMapper productionPlanMapper;

    /** Cria o objeto de domínio com quantidades, horário, estado e metadados de validação. */
    public ProductionExecution toDomain(ProductionExecutionEntity entity) {
        if (entity == null) return null;
        return new ProductionExecution(
                entity.getId(),
                productionPlanMapper.toDomain(entity.getProductionPlan()),
                entity.getQuantity(), entity.getQuantityKg(), entity.getHarvestedAt(), entity.getStatus(),
                entity.getValidatedBy(), entity.getValidatedAt(), entity.getNotes(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getLocationAccuracy(),
                entity.getLocationRecordedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    /** Prepara a entidade JPA com os campos atuais do apontamento. */
    public ProductionExecutionEntity toEntity(ProductionExecution domain) {
        if (domain == null) return null;
        ProductionExecutionEntity entity = new ProductionExecutionEntity();
        entity.setId(domain.getId());
        entity.setProductionPlan(productionPlanMapper.toEntity(domain.getProductionPlan()));
        entity.setQuantity(domain.getQuantity());
        entity.setQuantityKg(domain.getQuantityKg());
        entity.setHarvestedAt(domain.getHarvestedAt());
        entity.setStatus(domain.getStatus());
        entity.setValidatedBy(domain.getValidatedBy());
        entity.setValidatedAt(domain.getValidatedAt());
        entity.setNotes(domain.getNotes());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setLocationAccuracy(domain.getLocationAccuracy());
        entity.setLocationRecordedAt(domain.getLocationRecordedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
