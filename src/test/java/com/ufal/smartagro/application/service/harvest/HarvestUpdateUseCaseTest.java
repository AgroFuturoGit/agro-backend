package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.application.service.production.ProductionVersionGuard;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.ConcurrentUpdateException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HarvestUpdateUseCase — atualização e controle de concorrência")
class HarvestUpdateUseCaseTest {

    @Mock
    private HarvestRepository harvestRepository;

    private HarvestUpdateUseCase useCase;

    private static final UUID HARVEST_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new HarvestUpdateUseCase(harvestRepository, new ProductionVersionGuard());
    }

    private Harvest existingHarvest() {
        return new Harvest(
                HARVEST_ID,
                "Safra 2025/1",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30),
                LocalDateTime.of(2025, 1, 1, 8, 0, 0),
                LocalDateTime.of(2025, 1, 1, 8, 0, 0),
                null
        );
    }

    @Test
    @DisplayName("Atualização bem-sucedida preserva createdAt e atualiza rótulo/datas")
    void givenValidData_whenExecute_thenUpdatesSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        Harvest existing = existingHarvest();
        when(harvestRepository.findById(HARVEST_ID)).thenReturn(Optional.of(existing));
        when(harvestRepository.existsByLabel("Safra 2025/1 - Revisada")).thenReturn(false);
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(i -> i.getArgument(0));

        Harvest patchData = new Harvest(
                null,
                "Safra 2025/1 - Revisada",
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 7, 15)
        );

        Harvest updated = useCase.execute(HARVEST_ID, patchData, admin, existing.getUpdatedAt());

        assertThat(updated.getLabel()).isEqualTo("Safra 2025/1 - Revisada");
        assertThat(updated.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(updated.getEndDate()).isEqualTo(LocalDate.of(2025, 7, 15));
        assertThat(updated.getCreatedAt()).isEqualTo(existing.getCreatedAt());

        ArgumentCaptor<Harvest> captor = ArgumentCaptor.forClass(Harvest.class);
        verify(harvestRepository).save(captor.capture());
        assertThat(captor.getValue().getLabel()).isEqualTo("Safra 2025/1 - Revisada");
    }

    @Test
    @DisplayName("FARMER não tem permissão para atualizar safra")
    void givenFarmer_whenExecute_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        Harvest patchData = new Harvest(null, "Safra Invalida", LocalDate.now(), LocalDate.now().plusMonths(3));

        assertThatThrownBy(() -> useCase.execute(HARVEST_ID, patchData, farmer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Usuário não tem permissão para atualizar uma safra.");

        verify(harvestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Data de início após data de término lança IllegalArgumentException")
    void givenStartDateAfterEndDate_whenExecute_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        Harvest existing = existingHarvest();
        when(harvestRepository.findById(HARVEST_ID)).thenReturn(Optional.of(existing));

        Harvest patchData = new Harvest(
                null,
                "Safra Invalida",
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 1, 1)
        );

        assertThatThrownBy(() -> useCase.execute(HARVEST_ID, patchData, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de início não pode ser superior à data de término.");

        verify(harvestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rótulo duplicado lança HarvestAlreadyExistsException")
    void givenDuplicateLabel_whenExecute_thenThrowsHarvestAlreadyExistsException() {
        User admin = UserTestFactory.user().admin().build();
        Harvest existing = existingHarvest();
        when(harvestRepository.findById(HARVEST_ID)).thenReturn(Optional.of(existing));
        when(harvestRepository.existsByLabel("Safra Existente")).thenReturn(true);

        Harvest patchData = new Harvest(
                null,
                "Safra Existente",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30)
        );

        assertThatThrownBy(() -> useCase.execute(HARVEST_ID, patchData, admin))
                .isInstanceOf(HarvestAlreadyExistsException.class)
                .hasMessageContaining("Já existe uma safra com o rótulo");

        verify(harvestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Concorrência: lançar ConcurrentUpdateException quando baseUpdatedAt for diferente de updatedAt do servidor")
    void givenStaleBaseUpdatedAt_whenExecute_thenThrowsConcurrentUpdateException() {
        User admin = UserTestFactory.user().admin().build();
        LocalDateTime originalTime = LocalDateTime.of(2025, 1, 1, 8, 0, 0);
        LocalDateTime newerTime = LocalDateTime.of(2025, 1, 1, 9, 30, 0);

        Harvest existing = new Harvest(
                HARVEST_ID,
                "Safra 2025/1",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30),
                originalTime,
                newerTime,
                null
        );
        when(harvestRepository.findById(HARVEST_ID)).thenReturn(Optional.of(existing));

        Harvest patchData = new Harvest(
                null,
                "Safra 2025/1 Atualizada",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 6, 30)
        );

        assertThatThrownBy(() -> useCase.execute(HARVEST_ID, patchData, admin, originalTime))
                .isInstanceOf(ConcurrentUpdateException.class)
                .hasMessageContaining("A safra foi alterada por outro usuário");

        verify(harvestRepository, never()).save(any());
    }
}
