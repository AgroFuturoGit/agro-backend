package com.ufal.smartagro.application.service.crop;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CropDeleteUseCaseTest {

    @Mock
    private CropRepository cropRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private AuditLogService auditLogService;

    private CropDeleteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CropDeleteUseCase(cropRepository, productionPlanRepository, auditLogService);
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
        verifyNoInteractions(auditLogService);
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
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Quando não existem planos vinculados à cultura, deleta com sucesso e grava auditoria")
    void givenCropWithoutPlans_whenDelete_thenDeletesSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();
        Crop existingCrop = new Crop(cropId, "Milho", "Híbrido", true, 90, null, null, null, null);
        when(cropRepository.findById(cropId)).thenReturn(Optional.of(existingCrop));
        when(productionPlanRepository.countByCropId(cropId)).thenReturn(0L);
        when(productionPlanRepository.existsByCropId(cropId)).thenReturn(false);

        useCase.execute(cropId, admin);

        verify(cropRepository).deleteById(cropId);
        verify(auditLogService).record(eq(admin), eq(AuditAction.DELETE), eq("crops"), eq(cropId), any());
    }

    @Test
    @DisplayName("Quando usuário não é ADMIN nem TECHNICIAN, lança AccessDeniedException")
    void givenFarmer_whenDelete_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        UUID cropId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(cropId, farmer))
                .isInstanceOf(AccessDeniedException.class);

        verify(cropRepository, never()).deleteById(any());
        verifyNoInteractions(auditLogService);
    }
}
