package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CropRepository;
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
class CropDeleteUseCaseTest {

    @Mock
    private CropRepository cropRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    private CropDeleteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CropDeleteUseCase(cropRepository, productionPlanRepository);
    }

    @Test
    @DisplayName("Quando existem planos vinculados à cultura, lança EntityInUseException com a contagem e não deleta")
    void givenCropWithPlans_whenDelete_thenThrowsEntityInUseException() {
        User admin = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();
        when(cropRepository.findById(cropId)).thenReturn(Optional.of(mock(Crop.class)));
        when(productionPlanRepository.countByCropId(cropId)).thenReturn(3L);

        assertThatThrownBy(() -> useCase.execute(cropId, admin))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("3 planos de produção vinculados");

        verify(cropRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Quando a cultura não existe, lança CropNotFoundException")
    void givenNonExistentCrop_whenDelete_thenThrowsCropNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();
        when(cropRepository.findById(cropId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cropId, admin))
                .isInstanceOf(CropNotFoundException.class);

        verify(cropRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Quando não existem planos vinculados à cultura, deleta com sucesso")
    void givenCropWithoutPlans_whenDelete_thenDeletesSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();
        when(cropRepository.findById(cropId)).thenReturn(Optional.of(mock(Crop.class)));
        when(productionPlanRepository.countByCropId(cropId)).thenReturn(0L);
        when(productionPlanRepository.existsByCropId(cropId)).thenReturn(false);

        useCase.execute(cropId, admin);

        verify(cropRepository).deleteById(cropId);
    }

    @Test
    @DisplayName("Quando usuário não é ADMIN nem TECHNICIAN, lança AccessDeniedException")
    void givenFarmer_whenDelete_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        UUID cropId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(cropId, farmer))
                .isInstanceOf(AccessDeniedException.class);

        verify(cropRepository, never()).deleteById(any());
    }
}
