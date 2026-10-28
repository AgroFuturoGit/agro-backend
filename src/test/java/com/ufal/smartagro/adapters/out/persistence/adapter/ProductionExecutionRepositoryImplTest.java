package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.ProductionExecutionEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.ProductionExecutionMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaProductionExecutionRepository;
import com.ufal.smartagro.domain.model.ProductionExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionExecutionRepositoryImplTest {

    @Mock
    private JpaProductionExecutionRepository jpaRepository;

    @Mock
    private ProductionExecutionMapper productionExecutionMapper;

    @InjectMocks
    private ProductionExecutionRepositoryImpl repository;

    private UUID planId;
    private UUID executionId;
    private UUID clientId;
    private ProductionExecution domain;
    private ProductionExecutionEntity entity;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        executionId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        domain = mock(ProductionExecution.class);
        entity = mock(ProductionExecutionEntity.class);
    }

    @Test
    @DisplayName("save: deve mapear para entity, salvar e retornar domain")
    void save_deveSalvarEMapearRetorno() {
        when(productionExecutionMapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(productionExecutionMapper.toDomain(entity)).thenReturn(domain);

        ProductionExecution result = repository.save(domain);

        assertThat(result).isSameAs(domain);
        verify(productionExecutionMapper).toEntity(domain);
        verify(jpaRepository).save(entity);
        verify(productionExecutionMapper).toDomain(entity);
    }

    @Test
    @DisplayName("findById: deve delegar ao jpaRepository e mapear para domain")
    void findById_quandoEncontrado_deveRetornarDomain() {
        when(jpaRepository.findById(executionId)).thenReturn(Optional.of(entity));
        when(productionExecutionMapper.toDomain(entity)).thenReturn(domain);

        Optional<ProductionExecution> result = repository.findById(executionId);

        assertThat(result).containsSame(domain);
        verify(jpaRepository).findById(executionId);
        verify(productionExecutionMapper).toDomain(entity);
    }

    @Test
    @DisplayName("findAllByProductionPlanId: deve buscar pelo plano e mapear lista")
    void findAllByProductionPlanId_deveRetornarListaMapeada() {
        when(jpaRepository.findAllByProductionPlanId(planId)).thenReturn(List.of(entity));
        when(productionExecutionMapper.toDomain(entity)).thenReturn(domain);

        List<ProductionExecution> result = repository.findAllByProductionPlanId(planId);

        assertThat(result).containsExactly(domain);
        verify(jpaRepository).findAllByProductionPlanId(planId);
    }

    @Test
    @DisplayName("findByPlanIdAndClientId: deve buscar por planId e clientId e mapear")
    void findByPlanIdAndClientId_deveRetornarOptionalMapeado() {
        when(jpaRepository.findByProductionPlanIdAndClientId(planId, clientId)).thenReturn(Optional.of(entity));
        when(productionExecutionMapper.toDomain(entity)).thenReturn(domain);

        Optional<ProductionExecution> result = repository.findByPlanIdAndClientId(planId, clientId);

        assertThat(result).containsSame(domain);
        verify(jpaRepository).findByProductionPlanIdAndClientId(planId, clientId);
    }

    @Test
    @DisplayName("delete: deve invocar softDeleteById no repositório jpa")
    void delete_deveInvocarSoftDelete() {
        repository.delete(executionId);

        verify(jpaRepository).softDeleteById(executionId);
    }
}
