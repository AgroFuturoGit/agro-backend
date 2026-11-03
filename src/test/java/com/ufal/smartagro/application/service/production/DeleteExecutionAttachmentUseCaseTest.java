package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteExecutionAttachmentUseCaseTest {

    @Mock
    private ExecutionAttachmentRepository attachmentRepository;

    @Mock
    private ProductionExecutionRepository executionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;
    @Mock
    private AuditLogService auditLogService;

    private DeleteExecutionAttachmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteExecutionAttachmentUseCase(attachmentRepository, executionRepository, accessValidator, auditLogService);
    }

    @Test
    @DisplayName("Quando apontamento já validado e usuário é FARMER, lança AccessDeniedException ao remover anexo")
    void givenValidatedExecutionAndFarmer_thenThrowsAccessDeniedException() {
        UUID executionId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        ProductionPlan plan = mock(ProductionPlan.class);
        when(plan.getFarmer()).thenReturn(farmer);

        ProductionExecution execution = ProductionExecution.builder()
                .id(executionId)
                .productionPlan(plan)
                .status(ExecutionStatus.VALIDATED)
                .build();

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> useCase.delete(executionId, attachmentId, farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("já validado");

        verify(attachmentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando apontamento é PENDING e anexo pertence ao apontamento, exclui com sucesso")
    void givenPendingExecution_thenDeletesSuccessfully() {
        UUID executionId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        ProductionPlan plan = mock(ProductionPlan.class);
        when(plan.getFarmer()).thenReturn(farmer);

        ProductionExecution execution = ProductionExecution.builder()
                .id(executionId)
                .productionPlan(plan)
                .status(ExecutionStatus.PENDING)
                .build();

        ExecutionAttachment attachment = mock(ExecutionAttachment.class);
        when(attachment.getProductionExecutionId()).thenReturn(executionId);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        useCase.delete(executionId, attachmentId, farmerUser);

        verify(accessValidator).validateWriteAccess(farmer, farmerUser);
        verify(attachmentRepository).delete(attachmentId);
    }
}
