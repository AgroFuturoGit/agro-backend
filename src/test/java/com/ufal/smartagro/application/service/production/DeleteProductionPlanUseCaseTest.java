package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductionPlanUseCaseTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private ProductionExecutionRepository productionExecutionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;
    @Mock
    private AuditLogService auditLogService;

    private DeleteProductionPlanUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteProductionPlanUseCase(productionPlanRepository, productionExecutionRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Quando plano possui apontamentos vinculados, lança EntityInUseException (409)")
    void givenPlanWithExecutions_whenDelete_thenThrowsEntityInUseException() {
        UUID planId = UUID.randomUUID();
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = mock(ProductionPlan.class);
        when(plan.getFarmer()).thenReturn(farmer);

        when(productionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.countByProductionPlanId(planId)).thenReturn(1L);

        assertThatThrownBy(() -> useCase.delete(planId, admin))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("apontamentos de colheita vinculados");

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(productionPlanRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando plano não possui apontamentos vinculados, deleta com sucesso")
    void givenPlanWithoutExecutions_whenDelete_thenDeletesSuccessfully() {
        UUID planId = UUID.randomUUID();
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = mock(ProductionPlan.class);
        when(plan.getFarmer()).thenReturn(farmer);

        when(productionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.countByProductionPlanId(planId)).thenReturn(0L);

        useCase.delete(planId, admin);

        verify(accessValidator).validateWriteAccess(farmer, admin);
        verify(productionPlanRepository).delete(planId);
    }

    @Test
    @DisplayName("Quando plano não existe, lança EntityNotFoundException (404)")
    void givenNonExistentPlan_whenDelete_thenThrowsEntityNotFoundException() {
        UUID planId = UUID.randomUUID();
        User admin = UserTestFactory.user().admin().build();

        when(productionPlanRepository.findById(planId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(planId, admin))
                .isInstanceOf(EntityNotFoundException.class);

        verify(productionPlanRepository, never()).delete(any());
    }
}
