package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.ProductionPlanMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaProductionPlanRepository;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductionPlanRepositoryImpl implements ProductionPlanRepository {

    private final JpaProductionPlanRepository jpaRepository;
    private final ProductionPlanMapper productionPlanMapper;

    @Override
    public ProductionPlan save(ProductionPlan productionPlan) {
        var entity = productionPlanMapper.toEntity(productionPlan);
        var savedEntity = jpaRepository.save(entity);
        return productionPlanMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductionPlan> findById(UUID id) {
        return jpaRepository.findById(id).map(productionPlanMapper::toDomain);
    }

    @Override
    public java.util.List<ProductionPlan> findAllByProducerId(UUID producerId) {
        return jpaRepository.findAllByProducerId(producerId).stream()
                .map(productionPlanMapper::toDomain)
                .toList();
    }
}
