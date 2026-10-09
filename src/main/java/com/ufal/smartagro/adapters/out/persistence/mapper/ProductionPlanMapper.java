package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionPlanEntity;
import com.ufal.smartagro.domain.model.ProductionPlan;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/** Converte planos entre domínio e persistência, preservando snapshot e coordenadas. */
@Component
@RequiredArgsConstructor
public class ProductionPlanMapper {

    private final FarmerMapper farmerMapper;
    private final HarvestMapper harvestMapper;
    private final CropMapper cropMapper;

    /** Mapeia uma linha do banco para o plano usado pelas regras de negócio. */
    public ProductionPlan toDomain(ProductionPlanEntity entity) {
        if (entity == null) return null;
        return new ProductionPlan(
                entity.getId(),
                farmerMapper.toDomain(entity.getFarmer()),
                harvestMapper.toDomain(entity.getHarvest()),
                cropMapper.toDomain(entity.getCrop()),
                entity.getPlantedArea(),
                entity.getStatus(), entity.getExpectedHarvestStart(), entity.getExpectedHarvestEnd(), entity.getExpectedProductivity(),
                entity.getLocationDescription(), entity.getLatitude(), entity.getLongitude(),
                entity.getPlannedPlantingDate(),
                entity.getPlannedCalendar(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    /** Mapeia o plano completo para salvar ou atualizar no banco. */
    public ProductionPlanEntity toEntity(ProductionPlan domain) {
        if (domain == null) return null;
        ProductionPlanEntity entity = new ProductionPlanEntity();
        entity.setId(domain.getId());
        entity.setFarmer(farmerMapper.toEntity(domain.getFarmer()));
        entity.setHarvest(harvestMapper.toEntity(domain.getHarvest()));
        entity.setCrop(cropMapper.toEntity(domain.getCrop()));
        entity.setPlantedArea(domain.getPlantedArea());
        entity.setStatus(domain.getStatus());
        entity.setExpectedHarvestStart(domain.getExpectedHarvestStart());
        entity.setExpectedHarvestEnd(domain.getExpectedHarvestEnd());
        entity.setExpectedProductivity(domain.getExpectedProductivity());
        entity.setLocationDescription(domain.getLocationDescription());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setPlannedPlantingDate(domain.getPlannedPlantingDate());
        entity.setPlannedCalendar(domain.getPlannedCalendar());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
