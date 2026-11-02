package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductionExecutionUseCaseTest {

    @Mock
    private ProductionExecutionRepository productionExecutionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    @Mock
    private AuditLogService auditLogService;

    private DeleteProductionExecutionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteProductionExecutionUseCase(productionExecutionRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Quando agricultor tenta excluir apontamento já validado, lança AccessDeniedException (403)")
    void givenFarmerDeletingValidatedExecution_thenThrowsAccessDeniedException() {
        UUID executionId = UUID.randomUUID();
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        ProductionPlan plan = mock(ProductionPlan.class);
        when(plan.getFarmer()).thenReturn(farmer);

        ProductionExecution execution = ProductionExecution.builder()
                .id(executionId)
                .productionPlan(plan)
                .status(ExecutionStatus.VALIDATED)
                .quantityKg(BigDecimal.valueOf(150))
                .harvestedAt(LocalDate.now())
                .build();

        when(productionExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> useCase.delete(executionId, farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("já validado");

        verify(productionExecutionRepository, never()).delete(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Quando agricultor exclui apontamento pendente, exclui com sucesso")
    void givenFarmerDeletingPendingExecution_thenDeletesSuccessfully() {
        UUID executionId = UUID.randomUUID();
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        ProductionPlan plan = mock(ProductionPlan.class);
        when(plan.getFarmer()).thenReturn(farmer);

        ProductionExecution execution = ProductionExecution.builder()
                .id(executionId)
                .productionPlan(plan)
                .status(ExecutionStatus.PENDING)
                .quantityKg(BigDecimal.valueOf(100))
                .harvestedAt(LocalDate.now())
                .build();

        when(productionExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        useCase.delete(executionId, farmerUser);

        verify(accessValidator).validateWriteAccess(farmer, farmerUser);
        verify(productionExecutionRepository).delete(executionId);
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Quando técnico exclui apontamento validado, registra trilha de auditoria e exclui")
    void givenTechnicianDeletingValidatedExecution_thenAuditsAndDeletes() {
        UUID executionId = UUID.randomUUID();
        User techUser = UserTestFactory.user().technician().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = mock(ProductionPlan.class);
        when(plan.getFarmer()).thenReturn(farmer);

        ProductionExecution execution = ProductionExecution.builder()
                .id(executionId)
                .productionPlan(plan)
                .status(ExecutionStatus.VALIDATED)
                .quantityKg(BigDecimal.valueOf(250))
                .harvestedAt(LocalDate.now())
                .build();

        when(productionExecutionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        useCase.delete(executionId, techUser);

        verify(accessValidator).validateWriteAccess(farmer, techUser);
        verify(auditLogService).record(eq(techUser), eq(AuditAction.DELETE), eq("production_executions"), eq(executionId), any());
        verify(productionExecutionRepository).delete(executionId);
    }

    @Test
    @DisplayName("Quando apontamento não existe, lança EntityNotFoundException (404)")
    void givenNonExistentExecution_thenThrowsEntityNotFoundException() {
        UUID executionId = UUID.randomUUID();
        User admin = UserTestFactory.user().admin().build();

        when(productionExecutionRepository.findById(executionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(executionId, admin))
                .isInstanceOf(EntityNotFoundException.class);

        verify(productionExecutionRepository, never()).delete(any());
    }
}
