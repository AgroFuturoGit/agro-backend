package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.InvalidStateTransitionException;
import com.ufal.smartagro.domain.model.*;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.port.out.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RejectProductionExecutionUseCase — rejeição de apontamentos")
class RejectProductionExecutionUseCaseTest {

    @Mock
    private ProductionExecutionRepository productionExecutionRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;
    @Mock
    private com.ufal.smartagro.application.service.audit.AuditLogService auditLogService;

    private RejectProductionExecutionUseCase useCase;

    @BeforeEach
    void setUp() {
        ProductionAccessValidator validator = new ProductionAccessValidator(
                managerRepository, technicianRepository, technicalAssistanceRepository);
        useCase = new RejectProductionExecutionUseCase(productionExecutionRepository, validator, auditLogService);
    }

    private ProductionExecution createPendingExecution(Farmer farmer, String notes) {
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        return new ProductionExecution(
                UUID.randomUUID(),
                plan,
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                LocalDate.of(2026, 5, 10),
                ExecutionStatus.PENDING,
                null,
                null,
                notes,
                null, null, null, null,
                null, null, null
        );
    }

    @Test
    @DisplayName("FARMER não pode rejeitar apontamentos")
    void givenFarmer_whenReject_thenThrowsAccessDeniedException() {
        User farmerUser = UserTestFactory.user().farmer().build();
        UUID executionId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.reject(executionId, "Peso incorreto", farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O agricultor não tem permissão para rejeitar apontamentos.");

        verify(productionExecutionRepository, never()).save(any());
        verify(auditLogService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("ADMIN rejeita apontamento com justificativa gravada em rejectionReason e notas intactas")
    void givenAdmin_whenRejectWithReason_thenSucceedsAndSetsRejectionReason() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createPendingExecution(farmer, "Colheita matinal");

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecution result = useCase.reject(execution.getId(), "Peso informado diverge do romaneio", admin);

        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.REJECTED);
        assertThat(result.getValidatedBy()).isEqualTo(admin);
        assertThat(result.getValidatedAt()).isNotNull();
        assertThat(result.getNotes()).isEqualTo("Colheita matinal");
        assertThat(result.getRejectionReason()).isEqualTo("Peso informado diverge do romaneio");

        verify(auditLogService).record(
                eq(admin),
                eq(com.ufal.smartagro.domain.model.enums.AuditAction.REJECT),
                eq("production_executions"),
                eq(execution.getId()),
                argThat(map -> "PENDING".equals(map.get("previousStatus")) && "Peso informado diverge do romaneio".equals(map.get("reason")))
        );
    }

    @Test
    @DisplayName("ADMIN rejeita apontamento sem notas prévias mantendo notas nulas")
    void givenAdmin_whenRejectWithoutInitialNotes_thenSetsReason() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createPendingExecution(farmer, null);

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecution result = useCase.reject(execution.getId(), "Fora dos padrões", admin);

        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.REJECTED);
        assertThat(result.getNotes()).isNull();
        assertThat(result.getRejectionReason()).isEqualTo("Fora dos padrões");
    }

    @Test
    @DisplayName("ADMIN rejeita apontamento com motivo nulo ou em branco resultando em rejectionReason nulo")
    void givenAdmin_whenRejectWithBlankReason_thenRejectionReasonIsNull() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createPendingExecution(farmer, "Observação existente");

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecution result = useCase.reject(execution.getId(), "   ", admin);

        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.REJECTED);
        assertThat(result.getNotes()).isEqualTo("Observação existente");
        assertThat(result.getRejectionReason()).isNull();
    }

    @Test
    @DisplayName("Ao rejeitar apontamento que já não está PENDING, lança InvalidStateTransitionException")
    void givenNonPendingExecution_whenReject_thenThrowsInvalidStateTransitionException() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = UserTestFactory.productionPlan().farmer(farmer).build();
        ProductionExecution alreadyRejected = new ProductionExecution(
                UUID.randomUUID(), plan, new BigDecimal("10.00"), new BigDecimal("10.00"),
                LocalDate.of(2026, 5, 10), ExecutionStatus.REJECTED, null, null,
                "Observação", "Motivo", null, null, null, null, null, null, null
        );

        when(productionExecutionRepository.findById(alreadyRejected.getId())).thenReturn(Optional.of(alreadyRejected));

        assertThatThrownBy(() -> useCase.reject(alreadyRejected.getId(), "Novo motivo", admin))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Apenas apontamentos com status PENDENTE podem ser rejeitados");
    }

    @Test
    @DisplayName("TECHNICIAN com assistência ativa rejeita apontamento com sucesso")
    void givenTechnicianWithAssistance_whenReject_thenSucceeds() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community community = UserTestFactory.community().build();
        Farmer farmer = UserTestFactory.farmerProfile().community(community).build();
        ProductionExecution execution = createPendingExecution(farmer, null);

        when(productionExecutionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance()
                .technician(technician).community(community).build();
        when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), community.getId()))
                .thenReturn(Optional.of(assistance));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecution result = useCase.reject(execution.getId(), "Produto impróprio", techUser);

        assertThat(result.getStatus()).isEqualTo(ExecutionStatus.REJECTED);
        assertThat(result.getValidatedBy()).isEqualTo(techUser);
        assertThat(result.getValidatedAt()).isNotNull();
    }
}
