package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanRegisterDTO;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProductionPlanUseCase — validações e coerência temporal")
class CreateProductionPlanUseCaseTest {

    @Mock
    private ProductionPlanRepository productionPlanRepository;
    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private HarvestRepository harvestRepository;
    @Mock
    private CropRepository cropRepository;
    @Mock
    private ProductionAccessValidator accessValidator;

    private CreateProductionPlanUseCase useCase;

    private static final UUID FARMER_ID = UUID.randomUUID();
    private static final UUID HARVEST_ID = UUID.randomUUID();
    private static final UUID CROP_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CreateProductionPlanUseCase(
                productionPlanRepository,
                farmerRepository,
                harvestRepository,
                cropRepository,
                accessValidator
        );
    }

    private Harvest defaultHarvest() {
        return new Harvest(
                HARVEST_ID,
                "Safra 2026/1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );
    }

    private Crop defaultCrop() {
        return new Crop(
                CROP_ID,
                "Tomate",
                "Italiano",
                true,
                90,
                new BigDecimal("45000.00"),
                null, null, null
        );
    }

    @Test
    @DisplayName("Cria plano com sucesso quando todas as datas estão dentro da safra")
    void givenValidData_whenCreate_thenSavesPlan() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        when(farmerRepository.findById(FARMER_ID)).thenReturn(Optional.of(farmer));
        when(harvestRepository.findById(HARVEST_ID)).thenReturn(Optional.of(defaultHarvest()));
        when(cropRepository.findById(CROP_ID)).thenReturn(Optional.of(defaultCrop()));
        when(productionPlanRepository.save(any(ProductionPlan.class))).thenAnswer(i -> i.getArgument(0));

        ProductionPlanRegisterDTO dto = new ProductionPlanRegisterDTO(
                HARVEST_ID,
                CROP_ID,
                new BigDecimal("2.50"),
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 6, 1),
                "Talhão Sul",
                null, null,
                LocalDate.of(2026, 1, 15),
                null
        );

        ProductionPlan created = useCase.create(FARMER_ID, dto, admin);

        assertThat(created).isNotNull();
        assertThat(created.getPlantedArea()).isEqualByComparingTo("2.50");
        assertThat(created.getPlannedPlantingDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        verify(productionPlanRepository).save(any(ProductionPlan.class));
    }

    @Test
    @DisplayName("Data de plantio fora da safra lança IllegalArgumentException")
    void givenPlantingDateOutsideHarvest_whenCreate_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        when(farmerRepository.findById(FARMER_ID)).thenReturn(Optional.of(farmer));
        when(harvestRepository.findById(HARVEST_ID)).thenReturn(Optional.of(defaultHarvest()));
        when(cropRepository.findById(CROP_ID)).thenReturn(Optional.of(defaultCrop()));

        ProductionPlanRegisterDTO dto = new ProductionPlanRegisterDTO(
                HARVEST_ID,
                CROP_ID,
                new BigDecimal("2.50"),
                null, null, null, null, null,
                LocalDate.of(2025, 12, 1), // Antes de 2026-01-01
                null
        );

        assertThatThrownBy(() -> useCase.create(FARMER_ID, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A data de plantio deve estar dentro do período da safra");

        verify(productionPlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("Início da colheita posterior ao término lança IllegalArgumentException")
    void givenHarvestStartAfterEnd_whenCreate_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        when(farmerRepository.findById(FARMER_ID)).thenReturn(Optional.of(farmer));
        when(harvestRepository.findById(HARVEST_ID)).thenReturn(Optional.of(defaultHarvest()));
        when(cropRepository.findById(CROP_ID)).thenReturn(Optional.of(defaultCrop()));

        ProductionPlanRegisterDTO dto = new ProductionPlanRegisterDTO(
                HARVEST_ID,
                CROP_ID,
                new BigDecimal("2.50"),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 4, 1), // Invertido
                null, null, null, null, null
        );

        assertThatThrownBy(() -> useCase.create(FARMER_ID, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data prevista de início da colheita não pode ser posterior à data de término.");

        verify(productionPlanRepository, never()).save(any());
    }
}
