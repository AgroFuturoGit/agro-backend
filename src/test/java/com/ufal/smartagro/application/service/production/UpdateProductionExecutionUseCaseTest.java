package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.InvalidStateTransitionException;
import com.ufal.smartagro.domain.model.enums.AuditAction;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.*;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProductionExecutionUseCase — regras de validação, cálculo e autovalidação")
class UpdateProductionExecutionUseCaseTest {

    @Mock
    private ProductionExecutionRepository repository;
    @Mock
    private ProductionAccessValidator accessValidator;
    @Mock
    private ProductionVersionGuard versionGuard;
    @Mock
    private AuditLogService auditLogService;

    private UpdateProductionExecutionUseCase useCase;

    private static final UUID ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductionExecutionUseCase(repository, accessValidator, versionGuard, auditLogService);
    }

    private Crop cropBox15kg() {
        return new Crop(
                UUID.randomUUID(), "Tomate", "Italiano", false, 90,
                new BigDecimal("1000"), HarvestType.CONTINUOUS, MeasurementUnit.BOX, new BigDecimal("15.00")
        );
    }

    private ProductionExecution createExecution(Farmer farmer, ExecutionStatus status, User validator) {
        Crop crop = cropBox15kg();
        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, UserTestFactory.harvest(), crop,
                new BigDecimal("2.0"), com.ufal.smartagro.domain.model.enums.PlanStatus.ACTIVE,
                null, null, new BigDecimal("1000"), null, null, null, null, null, null, null, null
        );
        return new ProductionExecution(
                ID, plan, new BigDecimal("7"), new BigDecimal("105"),
                LocalDate.of(2026, 5, 10), status, validator,
                validator != null ? LocalDateTime.of(2026, 5, 11, 10, 0) : null,
                "Anotação existente", null, null, null, null,
                null, null, null
        );
    }

    @Test
    @DisplayName("FARMER não pode alterar status para VALIDATED via PUT")
    void givenFarmer_whenTriesToSelfValidate_thenThrowsAccessDeniedException() {
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        ProductionExecution execution = createExecution(farmer, ExecutionStatus.PENDING, null);

        when(repository.findById(ID)).thenReturn(Optional.of(execution));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                new BigDecimal("7"), new BigDecimal("105"), LocalDate.of(2026, 5, 10),
                ExecutionStatus.VALIDATED, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(ID, dto, farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O agricultor não tem permissão para alterar o status do apontamento.");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("FARMER alterando quantidade de apontamento já VALIDADO reverte status para PENDING")
    void givenValidatedExecution_whenFarmerEditsQuantity_thenRevertsToPending() {
        User farmerUser = UserTestFactory.user().farmer().build();
        User techValidator = UserTestFactory.user().technician().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        ProductionExecution execution = createExecution(farmer, ExecutionStatus.VALIDATED, techValidator);

        when(repository.findById(ID)).thenReturn(Optional.of(execution));
        when(repository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                new BigDecimal("10"), null, LocalDate.of(2026, 5, 10),
                null, null, null, null,
                null, null, null, null, null, null
        );

        ProductionExecution updated = useCase.update(ID, dto, farmerUser);

        assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        assertThat(updated.getValidatedBy()).isNull();
        assertThat(updated.getValidatedAt()).isNull();
        assertThat(updated.getQuantity()).isEqualByComparingTo(new BigDecimal("10"));
        // 10 caixas * 15 kg = 150 kg
        assertThat(updated.getQuantityKg()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("String vazia no campo notes limpa as observações (notes = null)")
    void givenEmptyNotes_whenUpdate_thenClearsNotes() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createExecution(farmer, ExecutionStatus.PENDING, null);

        when(repository.findById(ID)).thenReturn(Optional.of(execution));
        when(repository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                null, null, null, null, null, null, "   ",
                null, null, null, null, null, null
        );

        ProductionExecution updated = useCase.update(ID, dto, admin);

        assertThat(updated.getNotes()).isNull();
    }

    @Test
    @DisplayName("Data da colheita anterior ao plantio lanca IllegalArgumentException")
    void givenHarvestedAtBeforePlanting_whenUpdate_thenThrowsException() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        Harvest harvest = new Harvest(UUID.randomUUID(), "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        Crop crop = cropBox15kg();
        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, harvest, crop,
                new BigDecimal("2.0"), com.ufal.smartagro.domain.model.enums.PlanStatus.ACTIVE,
                null, null, new BigDecimal("1000"), null, null, null, LocalDate.of(2026, 3, 1), null, null, null, null
        );
        ProductionExecution execution = new ProductionExecution(
                ID, plan, new BigDecimal("7"), new BigDecimal("105"),
                LocalDate.of(2026, 5, 10), ExecutionStatus.PENDING, null, null,
                null, null, null, null, null, null, null, null
        );
        when(repository.findById(ID)).thenReturn(Optional.of(execution));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                null, null, LocalDate.of(2026, 2, 28), null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(ID, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data da colheita não pode ser anterior à data planejada de plantio.");
    }

    @Test
    @DisplayName("Data da colheita anterior ao inicio da safra lanca IllegalArgumentException")
    void givenHarvestedAtBeforeHarvestStart_whenUpdate_thenThrowsException() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        Harvest harvest = new Harvest(UUID.randomUUID(), "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        Crop crop = cropBox15kg();
        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, harvest, crop,
                new BigDecimal("2.0"), com.ufal.smartagro.domain.model.enums.PlanStatus.ACTIVE,
                null, null, new BigDecimal("1000"), null, null, null, null, null, null, null, null
        );
        ProductionExecution execution = new ProductionExecution(
                ID, plan, new BigDecimal("7"), new BigDecimal("105"),
                LocalDate.of(2026, 5, 10), ExecutionStatus.PENDING, null, null,
                null, null, null, null, null, null, null, null
        );
        when(repository.findById(ID)).thenReturn(Optional.of(execution));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                null, null, LocalDate.of(2025, 12, 31), null, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(ID, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data da colheita não pode ser anterior ao início da safra.");
    }

    @Test
    @DisplayName("Tentativa de validar execucao ja REJEITADA via PUT lanca InvalidStateTransitionException")
    void givenRejectedExecution_whenValidateViaPut_thenThrowsInvalidStateTransitionException() {
        User tech = UserTestFactory.user().technician().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createExecution(farmer, ExecutionStatus.REJECTED, tech);
        when(repository.findById(ID)).thenReturn(Optional.of(execution));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                null, null, null, ExecutionStatus.VALIDATED, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(ID, dto, tech))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Apenas apontamentos com status PENDENTE podem ser validados.");
    }

    @Test
    @DisplayName("Tentativa de rejeitar execucao ja VALIDADA via PUT lanca InvalidStateTransitionException")
    void givenValidatedExecution_whenRejectViaPut_thenThrowsInvalidStateTransitionException() {
        User tech = UserTestFactory.user().technician().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createExecution(farmer, ExecutionStatus.VALIDATED, tech);
        when(repository.findById(ID)).thenReturn(Optional.of(execution));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                null, null, null, ExecutionStatus.REJECTED, null, null, null,
                null, null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.update(ID, dto, tech))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Apenas apontamentos com status PENDENTE podem ser rejeitados.");
    }

    @Test
    @DisplayName("Validacao de execucao PENDENTE via PUT registra AuditAction.VALIDATE")
    void givenPendingExecution_whenValidateViaPut_thenAuditsValidation() {
        User tech = UserTestFactory.user().technician().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createExecution(farmer, ExecutionStatus.PENDING, null);
        when(repository.findById(ID)).thenReturn(Optional.of(execution));
        when(repository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                null, null, null, ExecutionStatus.VALIDATED, null, null, null,
                null, null, null, null, null, null
        );

        ProductionExecution updated = useCase.update(ID, dto, tech);

        assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.VALIDATED);
        assertThat(updated.getValidatedBy()).isEqualTo(tech);
        assertThat(updated.getValidatedAt()).isNotNull();
        verify(auditLogService).record(eq(tech), eq(AuditAction.VALIDATE), eq("production_executions"), eq(ID), any());
    }

    @Test
    @DisplayName("Rejeicao de execucao PENDENTE via PUT registra AuditAction.REJECT")
    void givenPendingExecution_whenRejectViaPut_thenAuditsRejection() {
        User tech = UserTestFactory.user().technician().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionExecution execution = createExecution(farmer, ExecutionStatus.PENDING, null);
        when(repository.findById(ID)).thenReturn(Optional.of(execution));
        when(repository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecutionUpdateDTO dto = new ProductionExecutionUpdateDTO(
                null, null, null, ExecutionStatus.REJECTED, null, null, "Qualidade inadequada",
                null, null, null, null, null, null
        );

        ProductionExecution updated = useCase.update(ID, dto, tech);

        assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.REJECTED);
        assertThat(updated.getValidatedBy()).isEqualTo(tech);
        assertThat(updated.getRejectionReason()).isEqualTo("Qualidade inadequada");
        verify(auditLogService).record(eq(tech), eq(AuditAction.REJECT), eq("production_executions"), eq(ID), any());
    }
}
