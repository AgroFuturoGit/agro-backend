package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
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
@DisplayName("UpdateProductionPlanUseCase — limpeza de campos, coerência de datas e updatedAt (API-19)")
class UpdateProductionPlanUseCaseTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private ProductionAccessValidator accessValidator;
    @Mock
    private ProductionVersionGuard versionGuard;
    @Mock
    private AuditLogService auditLogService;

    private UpdateProductionPlanUseCase useCase;

    private static final UUID PLAN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductionPlanUseCase(
                productionPlanRepository,
                accessValidator,
                versionGuard,
                auditLogService
        );
    }

    private ProductionPlan existingPlan() {
        Harvest harvest = new Harvest(UUID.randomUUID(), "Safra 2026/1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        Farmer farmer = UserTestFactory.farmerProfile().build();
        Crop crop = new Crop(UUID.randomUUID(), "Milho", "Híbrido", false, 90, new BigDecimal("10.00"), null, null, null);

        return new ProductionPlan(
                PLAN_ID, farmer, harvest, crop, new BigDecimal("3.00"), PlanStatus.ACTIVE,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 1), new BigDecimal("10.00"),
                "Talhão A", null, null, LocalDate.of(2026, 1, 15), null, null, null, null
        );
    }

    @Test
    @DisplayName("clearPlannedPlantingDate = true limpa a data de plantio planejada e preenche updatedAt (API-19)")
    void givenClearPlannedPlantingDate_whenUpdate_thenClearsPlantingDateAndPopulatesUpdatedAt() {
        User admin = UserTestFactory.user().admin().build();
        ProductionPlan existing = existingPlan();
        when(productionPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(existing));
        when(productionPlanRepository.save(any(ProductionPlan.class))).thenAnswer(i -> i.getArgument(0));

        ProductionPlanUpdateDTO dto = new ProductionPlanUpdateDTO(
                null, null, null, null, null, null, null, null, null, null,
                false, // clearHarvestDates
                true,  // clearPlannedPlantingDate
                null   // baseUpdatedAt
        );

        ProductionPlan updated = useCase.update(PLAN_ID, dto, admin);

        assertThat(updated.getPlannedPlantingDate()).isNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
        ArgumentCaptor<ProductionPlan> captor = ArgumentCaptor.forClass(ProductionPlan.class);
        verify(productionPlanRepository).save(captor.capture());
        assertThat(captor.getValue().getPlannedPlantingDate()).isNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Data de início da colheita posterior ao término lança IllegalArgumentException")
    void givenHarvestStartAfterEnd_whenUpdate_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        ProductionPlan existing = existingPlan();
        when(productionPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(existing));

        ProductionPlanUpdateDTO dto = new ProductionPlanUpdateDTO(
                null, null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 3, 1), // Invertido
                null, null, null, null, null, null, false, false, null
        );

        assertThatThrownBy(() -> useCase.update(PLAN_ID, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data prevista de início da colheita não pode ser posterior à data de término.");

        verify(productionPlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Data de plantio fora do período da safra lança IllegalArgumentException")
    void givenPlantingDateOutsideHarvestWindow_whenUpdate_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        ProductionPlan existing = existingPlan();
        when(productionPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(existing));

        // existingPlan harvest is 2026-01-01 to 2026-06-30
        ProductionPlanUpdateDTO dto = new ProductionPlanUpdateDTO(
                null, null, null, null, null, null, null, null,
                LocalDate.of(2025, 12, 31), // Fora da safra
                null, false, false, null
        );

        assertThatThrownBy(() -> useCase.update(PLAN_ID, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A data de plantio deve estar dentro do período da safra");

        verify(productionPlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Data de plantio posterior ao início da colheita lança IllegalArgumentException")
    void givenPlantingDateAfterHarvestStart_whenUpdate_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        ProductionPlan existing = existingPlan();
        when(productionPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(existing));

        // existingPlan harvestStart is 2026-04-01
        ProductionPlanUpdateDTO dto = new ProductionPlanUpdateDTO(
                null, null, null, null, null, null, null, null,
                LocalDate.of(2026, 4, 15), // Posterior ao harvestStart
                null, false, false, null
        );

        assertThatThrownBy(() -> useCase.update(PLAN_ID, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data planejada de plantio não pode ser posterior ao início previsto da colheita.");

        verify(productionPlanRepository, never()).save(any());
    }
}
