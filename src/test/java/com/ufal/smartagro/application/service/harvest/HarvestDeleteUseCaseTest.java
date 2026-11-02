package com.ufal.smartagro.application.service.harvest;

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
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
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
class HarvestDeleteUseCaseTest {

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    @Mock
    private AuditLogService auditLogService;

    private HarvestDeleteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HarvestDeleteUseCase(harvestRepository, productionPlanRepository, auditLogService);
    }

    @Test
    @DisplayName("Quando existem planos vinculados à safra, lança EntityInUseException com a contagem e não deleta")
    void givenHarvestWithPlans_whenDelete_thenThrowsEntityInUseException() {
        User admin = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(mock(Harvest.class)));
        when(productionPlanRepository.countByHarvestId(harvestId)).thenReturn(2L);

        assertThatThrownBy(() -> useCase.execute(harvestId, admin))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("2 planos de produção vinculados");

        verify(harvestRepository, never()).deleteById(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Quando a safra não existe, lança HarvestNotFoundException")
    void givenNonExistentHarvest_whenDelete_thenThrowsHarvestNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        when(harvestRepository.findById(harvestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(harvestId, admin))
                .isInstanceOf(HarvestNotFoundException.class);

        verify(harvestRepository, never()).deleteById(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Quando não existem planos vinculados à safra, deleta com sucesso e grava auditoria")
    void givenHarvestWithoutPlans_whenDelete_thenDeletesSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        Harvest existing = new Harvest(harvestId, "Safra 2026/1", null, null);
        when(harvestRepository.findById(harvestId)).thenReturn(Optional.of(existing));
        when(productionPlanRepository.countByHarvestId(harvestId)).thenReturn(0L);
        when(productionPlanRepository.existsByHarvestId(harvestId)).thenReturn(false);

        useCase.execute(harvestId, admin);

        verify(harvestRepository).deleteById(harvestId);
        verify(auditLogService).record(eq(admin), eq(AuditAction.DELETE), eq("harvests"), eq(harvestId), any());
    }

    @Test
    @DisplayName("Quando usuário não é ADMIN nem TECHNICIAN, lança AccessDeniedException")
    void givenFarmer_whenDelete_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        UUID harvestId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(harvestId, farmer))
                .isInstanceOf(AccessDeniedException.class);

        verify(harvestRepository, never()).deleteById(any());
        verifyNoInteractions(auditLogService);
    }
}
