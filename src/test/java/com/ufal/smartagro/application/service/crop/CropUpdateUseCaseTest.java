package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CropUpdateUseCase — atualização com semântica de PATCH")
class CropUpdateUseCaseTest {

    @Mock
    private CropRepository cropRepository;

    private CropUpdateUseCase useCase;

    private static final UUID CROP_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CropUpdateUseCase(cropRepository);
    }

    private Crop existingCrop() {
        return new Crop(
                CROP_ID,
                "Tomate",
                "Italiano",
                true,
                90,
                new BigDecimal("45000.00"),
                HarvestType.CONTINUOUS,
                MeasurementUnit.BOX,
                new BigDecimal("15.00")
        );
    }

    @Test
    @DisplayName("PATCH: enviar apenas nome preserva ciclo, produtividade, tipo, unidade e peso")
    void givenPartialUpdateWithOnlyName_whenExecute_thenPreservesOtherFields() {
        User admin = UserTestFactory.user().admin().build();
        Crop existing = existingCrop();
        when(cropRepository.findById(CROP_ID)).thenReturn(Optional.of(existing));
        when(cropRepository.existsByNameAndVariety("Tomate Especial", "Italiano")).thenReturn(false);
        when(cropRepository.save(any(Crop.class))).thenAnswer(i -> i.getArgument(0));

        Crop patchData = new Crop(null, "Tomate Especial", null, null, null, null, null, null, null);

        Crop updated = useCase.execute(CROP_ID, patchData, admin);

        assertThat(updated.getName()).isEqualTo("Tomate Especial");
        assertThat(updated.getVariety()).isEqualTo("Italiano");
        assertThat(updated.getIsPriority()).isTrue();
        assertThat(updated.getCycleDays()).isEqualTo(90);
        assertThat(updated.getExpectedProductivity()).isEqualByComparingTo(new BigDecimal("45000.00"));
        assertThat(updated.getHarvestType()).isEqualTo(HarvestType.CONTINUOUS);
        assertThat(updated.getUnit()).isEqualTo(MeasurementUnit.BOX);
        assertThat(updated.getUnitWeightKg()).isEqualByComparingTo(new BigDecimal("15.00"));

        ArgumentCaptor<Crop> captor = ArgumentCaptor.forClass(Crop.class);
        verify(cropRepository).save(captor.capture());
        assertThat(captor.getValue().getCycleDays()).isEqualTo(90);
    }

    @Test
    @DisplayName("PATCH: enviar apenas cycleDays preserva nome e demais atributos")
    void givenPartialUpdateWithOnlyCycleDays_whenExecute_thenPreservesOtherFields() {
        User admin = UserTestFactory.user().admin().build();
        Crop existing = existingCrop();
        when(cropRepository.findById(CROP_ID)).thenReturn(Optional.of(existing));
        when(cropRepository.save(any(Crop.class))).thenAnswer(i -> i.getArgument(0));

        Crop patchData = new Crop(null, null, null, null, 120, null, null, null, null);

        Crop updated = useCase.execute(CROP_ID, patchData, admin);

        assertThat(updated.getName()).isEqualTo("Tomate");
        assertThat(updated.getVariety()).isEqualTo("Italiano");
        assertThat(updated.getCycleDays()).isEqualTo(120);
        assertThat(updated.getUnitWeightKg()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("FARMER não tem permissão para alterar culturas")
    void givenFarmer_whenExecute_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        Crop patchData = new Crop(null, "Novo Nome", null, null);

        assertThatThrownBy(() -> useCase.execute(CROP_ID, patchData, farmer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("User does not have permission to update a crop.");

        verify(cropRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nome e variedade duplicados lançam CropAlreadyExistsException")
    void givenDuplicateNameAndVariety_whenExecute_thenThrowsException() {
        User admin = UserTestFactory.user().admin().build();
        Crop existing = existingCrop();
        when(cropRepository.findById(CROP_ID)).thenReturn(Optional.of(existing));
        when(cropRepository.existsByNameAndVariety("Cebola", "Roxa")).thenReturn(true);

        Crop patchData = new Crop(null, "Cebola", "Roxa", false);

        assertThatThrownBy(() -> useCase.execute(CROP_ID, patchData, admin))
                .isInstanceOf(CropAlreadyExistsException.class);

        verify(cropRepository, never()).save(any());
    }
}
