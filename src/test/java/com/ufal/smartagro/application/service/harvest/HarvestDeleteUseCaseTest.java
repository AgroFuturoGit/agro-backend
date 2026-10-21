package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestDeleteUseCaseTest {

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private ProductionPlanRepository productionPlanRepository;

    private HarvestDeleteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HarvestDeleteUseCase(harvestRepository, productionPlanRepository);
    }

    @Test
    @DisplayName("Quando existem planos vinculados à safra, lança EntityInUseException e não deleta")
    void givenHarvestWithPlans_whenDelete_thenThrowsEntityInUseException() {
        User admin = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        when(productionPlanRepository.existsByHarvestId(harvestId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(harvestId, admin))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("planos de produção vinculados");

        verify(harvestRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Quando não existem planos vinculados à safra, deleta com sucesso")
    void givenHarvestWithoutPlans_whenDelete_thenDeletesSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        when(productionPlanRepository.existsByHarvestId(harvestId)).thenReturn(false);

        useCase.execute(harvestId, admin);

        verify(harvestRepository).deleteById(harvestId);
    }

    @Test
    @DisplayName("Quando usuário não é ADMIN nem TECHNICIAN, lança AccessDeniedException")
    void givenFarmer_whenDelete_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        UUID harvestId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(harvestId, farmer))
                .isInstanceOf(AccessDeniedException.class);

        verify(harvestRepository, never()).deleteById(any());
    }
}
