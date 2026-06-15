package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.mapper.ProductionExecutionMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaProductionExecutionRepository;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductionExecutionRepositoryImpl implements ProductionExecutionRepository {

    private final JpaProductionExecutionRepository jpaRepository;
    private final ProductionExecutionMapper productionExecutionMapper;

    @Override
    public ProductionExecution save(ProductionExecution productionExecution) {
        var entity = productionExecutionMapper.toEntity(productionExecution);
        var savedEntity = jpaRepository.save(entity);
        return productionExecutionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductionExecution> findById(UUID id) {
        return jpaRepository.findById(id).map(productionExecutionMapper::toDomain);
    }
}
