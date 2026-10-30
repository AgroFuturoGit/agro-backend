package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanUpdateDTO;
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
@DisplayName("UpdateProductionPlanUseCase — limpeza de campos e coerência de datas")
class UpdateProductionPlanUseCaseTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private ProductionAccessValidator accessValidator;
    @Mock
    private ProductionVersionGuard versionGuard;

    private UpdateProductionPlanUseCase useCase;

    private static final UUID PLAN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductionPlanUseCase(
                productionPlanRepository,
                accessValidator,
                versionGuard
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
    @DisplayName("clearPlannedPlantingDate = true limpa a data de plantio planejada")
    void givenClearPlannedPlantingDate_whenUpdate_thenClearsPlantingDate() {
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
        ArgumentCaptor<ProductionPlan> captor = ArgumentCaptor.forClass(ProductionPlan.class);
        verify(productionPlanRepository).save(captor.capture());
        assertThat(captor.getValue().getPlannedPlantingDate()).isNull();
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
}
