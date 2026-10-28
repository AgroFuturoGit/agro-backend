package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductionExecutionUseCaseTest {

    @Mock
    private ProductionExecutionRepository productionExecutionRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private CreateProductionExecutionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateProductionExecutionUseCase(
                productionExecutionRepository,
                productionPlanRepository,
                accessValidator
        );
    }

    @Test
    @DisplayName("Quando plano está PLANNED, cria apontamento PENDING e transiciona plano para ACTIVE")
    void givenPlannedPlan_whenCreateExecution_thenTransitionsPlanToActive() {
        User loggedUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        Crop crop = new Crop(
                UUID.randomUUID(), "Milho", "Híbrido", true, 90,
                new BigDecimal("5000.00"), null, MeasurementUnit.KG, BigDecimal.ONE
        );

        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, null, crop, new BigDecimal("2.0"),
                PlanStatus.PLANNED, null, null, new BigDecimal("5000.00"),
                null, null, null, null, null, null, null, null
        );

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                LocalDate.of(2026, 6, 1),
                "Primeira colheita",
                null, null, null, null
        );

        ProductionExecution created = useCase.create(plan.getId(), dto, loggedUser);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        assertThat(created.getQuantityKg()).isEqualByComparingTo("50.00");

        ArgumentCaptor<ProductionPlan> planCaptor = ArgumentCaptor.forClass(ProductionPlan.class);
        verify(productionPlanRepository).save(planCaptor.capture());
        ProductionPlan savedPlan = planCaptor.getValue();
        assertThat(savedPlan.getStatus()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    @DisplayName("Quando plano já está ACTIVE, cria apontamento sem alterar status do plano")
    void givenActivePlan_whenCreateExecution_thenDoesNotSavePlanAgain() {
        User loggedUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        Crop crop = new Crop(
                UUID.randomUUID(), "Milho", "Híbrido", true, 90,
                new BigDecimal("5000.00"), null, MeasurementUnit.KG, BigDecimal.ONE
        );

        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, null, crop, new BigDecimal("2.0"),
                PlanStatus.ACTIVE, null, null, new BigDecimal("5000.00"),
                null, null, null, null, null, null, null, null
        );

        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                new BigDecimal("30.00"),
                new BigDecimal("30.00"),
                LocalDate.of(2026, 6, 15),
                "Segunda colheita",
                null, null, null, null
        );

        ProductionExecution created = useCase.create(plan.getId(), dto, loggedUser);

        assertThat(created).isNotNull();
        assertThat(created.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        verify(productionPlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Data de colheita no futuro lança IllegalArgumentException")
    void givenFutureHarvestDate_whenCreate_thenThrowsIllegalArgumentException() {
        User loggedUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, null, null, new BigDecimal("2.0"),
                PlanStatus.ACTIVE, null, null, new BigDecimal("5000.00"),
                null, null, null, null, null, null, null, null
        );
        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                LocalDate.now().plusDays(2),
                null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.create(plan.getId(), dto, loggedUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data da colheita não pode ser futura.");
    }

    @Test
    @DisplayName("Data de colheita anterior a 1970 lança IllegalArgumentException")
    void givenDateBefore1970_whenCreate_thenThrowsIllegalArgumentException() {
        User loggedUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, null, null, new BigDecimal("2.0"),
                PlanStatus.ACTIVE, null, null, new BigDecimal("5000.00"),
                null, null, null, null, null, null, null, null
        );
        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                LocalDate.of(1969, 12, 31),
                null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.create(plan.getId(), dto, loggedUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data da colheita não pode ser anterior a 1970.");
    }

    @Test
    @DisplayName("Data de colheita anterior ao plantio planejado lança IllegalArgumentException")
    void givenHarvestDateBeforePlanting_whenCreate_thenThrowsIllegalArgumentException() {
        User loggedUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, null, null, new BigDecimal("2.0"),
                PlanStatus.ACTIVE, null, null, new BigDecimal("5000.00"),
                null, null, null, LocalDate.of(2026, 5, 10), null, null, null, null
        );
        when(productionPlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));

        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                LocalDate.of(2026, 5, 1),
                null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.create(plan.getId(), dto, loggedUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data da colheita não pode ser anterior à data planejada de plantio.");
    }

    @Test
    @DisplayName("Quando clientId já existe para o plano, retorna execução existente sem salvar novamente (idempotência)")
    void givenExistingClientId_whenCreate_thenReturnsExistingExecutionWithoutSaving() {
        User loggedUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID planId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        ProductionPlan plan = new ProductionPlan(
                planId, farmer, null, null, new BigDecimal("2.0"),
                PlanStatus.ACTIVE, null, null, new BigDecimal("5000.00"),
                null, null, null, null, null, null, null, null
        );
        ProductionExecution existingExecution = ProductionExecution.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .productionPlan(plan)
                .quantity(new BigDecimal("20.00"))
                .quantityKg(new BigDecimal("20.00"))
                .status(ExecutionStatus.PENDING)
                .build();

        when(productionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findByPlanIdAndClientId(planId, clientId))
                .thenReturn(Optional.of(existingExecution));

        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                clientId, new BigDecimal("20.00"), new BigDecimal("20.00"),
                LocalDate.of(2026, 6, 1), "Colheita", null, null, null, null
        );

        ProductionExecution result = useCase.create(planId, dto, loggedUser);

        assertThat(result).isSameAs(existingExecution);
        verify(accessValidator).validateWriteAccess(farmer, loggedUser);
        verify(productionExecutionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Quando clientId é novo, persiste o apontamento vinculando o clientId")
    void givenNewClientId_whenCreate_thenSavesExecutionWithClientId() {
        User loggedUser = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID planId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        ProductionPlan plan = new ProductionPlan(
                planId, farmer, null, null, new BigDecimal("2.0"),
                PlanStatus.ACTIVE, null, null, new BigDecimal("5000.00"),
                null, null, null, null, null, null, null, null
        );

        when(productionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(productionExecutionRepository.findByPlanIdAndClientId(planId, clientId))
                .thenReturn(Optional.empty());
        when(productionExecutionRepository.save(any(ProductionExecution.class))).thenAnswer(i -> i.getArgument(0));

        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                clientId, new BigDecimal("20.00"), new BigDecimal("20.00"),
                LocalDate.of(2026, 6, 1), "Colheita", null, null, null, null
        );

        ProductionExecution result = useCase.create(planId, dto, loggedUser);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(clientId);
        verify(productionExecutionRepository).save(any(ProductionExecution.class));
    }
}
