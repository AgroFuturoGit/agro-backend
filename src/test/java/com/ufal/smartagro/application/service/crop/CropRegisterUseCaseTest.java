package com.ufal.smartagro.application.service.crop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CropRegisterUseCase — cadastro de culturas agrícolas e RBAC")
class CropRegisterUseCaseTest {

    @Mock
    private CropRepository cropRepository;

    @Mock
    private AuditLogService auditLogService;

    private CropRegisterUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CropRegisterUseCase(cropRepository, auditLogService);
    }

    @Test
    @DisplayName("ADMIN cadastra cultura com sucesso e grava trilha de auditoria")
    void givenAdmin_whenRegister_thenSucceeds() {
        User admin = UserTestFactory.user().admin().build();
        Crop crop = new Crop(null, "Milho", "Híbrido", true, 90, new BigDecimal("8000.00"), HarvestType.SINGLE, MeasurementUnit.KG, new BigDecimal("0.25"));

        when(cropRepository.existsByNameAndVariety("Milho", "Híbrido")).thenReturn(false);
        when(cropRepository.save(any(Crop.class))).thenAnswer(i -> {
            Crop c = i.getArgument(0);
            return new Crop(UUID.randomUUID(), c.getName(), c.getVariety(), c.getIsPriority(), c.getCycleDays(), c.getExpectedProductivity(), c.getHarvestType(), c.getUnit(), c.getUnitWeightKg());
        });

        Crop saved = useCase.execute(crop, admin);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Milho");
        assertThat(saved.getVariety()).isEqualTo("Híbrido");
        verify(cropRepository).save(any(Crop.class));
        verify(auditLogService).record(eq(admin), eq(AuditAction.CREATE), eq("crops"), eq(saved.getId()), any());
    }

    @Test
    @DisplayName("TECHNICIAN cadastra cultura com sucesso")
    void givenTechnician_whenRegister_thenSucceeds() {
        User tech = UserTestFactory.user().technician().build();
        Crop crop = new Crop(null, "Feijão", "Carioca", true, 60, new BigDecimal("2500.00"), HarvestType.CONTINUOUS, MeasurementUnit.KG, new BigDecimal("0.10"));

        when(cropRepository.existsByNameAndVariety("Feijão", "Carioca")).thenReturn(false);
        when(cropRepository.save(any(Crop.class))).thenAnswer(i -> {
            Crop c = i.getArgument(0);
            return new Crop(UUID.randomUUID(), c.getName(), c.getVariety(), c.getIsPriority(), c.getCycleDays(), c.getExpectedProductivity(), c.getHarvestType(), c.getUnit(), c.getUnitWeightKg());
        });

        Crop saved = useCase.execute(crop, tech);

        assertThat(saved).isNotNull();
        verify(cropRepository).save(any(Crop.class));
        verify(auditLogService).record(eq(tech), eq(AuditAction.CREATE), eq("crops"), eq(saved.getId()), any());
    }

    @Test
    @DisplayName("FARMER não tem permissão para cadastrar cultura")
    void givenFarmer_whenRegister_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        Crop crop = new Crop(null, "Milho", "Híbrido", true, 90, null, null, null, null);

        assertThatThrownBy(() -> useCase.execute(crop, farmer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Usuário não tem permissão para cadastrar culturas.");

        verify(cropRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Cultura duplicada por nome e variedade lança CropAlreadyExistsException")
    void givenDuplicateNameAndVariety_whenRegister_thenThrowsCropAlreadyExistsException() {
        User admin = UserTestFactory.user().admin().build();
        Crop crop = new Crop(null, "Milho", "Híbrido", true, 90, null, null, null, null);

        when(cropRepository.existsByNameAndVariety("Milho", "Híbrido")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(crop, admin))
                .isInstanceOf(CropAlreadyExistsException.class);

        verify(cropRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }
}
