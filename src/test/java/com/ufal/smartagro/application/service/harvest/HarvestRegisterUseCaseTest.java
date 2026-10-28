package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HarvestRegisterUseCase — cadastro e validações de coerência temporal")
class HarvestRegisterUseCaseTest {

    @Mock
    private HarvestRepository harvestRepository;

    private HarvestRegisterUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HarvestRegisterUseCase(harvestRepository);
    }

    @Test
    @DisplayName("Cadastro com dados válidos persiste nova safra")
    void givenValidData_whenRegister_thenSavesHarvest() {
        User admin = UserTestFactory.user().admin().build();
        Harvest harvest = new Harvest(
                null, "Safra 2026/1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );

        when(harvestRepository.existsByLabel("Safra 2026/1")).thenReturn(false);
        when(harvestRepository.findAll()).thenReturn(List.of());
        when(harvestRepository.save(any(Harvest.class))).thenAnswer(i -> i.getArgument(0));

        Harvest saved = useCase.execute(harvest, admin);

        assertThat(saved.getLabel()).isEqualTo("Safra 2026/1");
        verify(harvestRepository).save(any(Harvest.class));
    }

    @Test
    @DisplayName("Data de início após data de término lança IllegalArgumentException")
    void givenStartDateAfterEndDate_whenRegister_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        Harvest harvest = new Harvest(
                null, "Safra Invalida",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 6, 30)
        );

        assertThatThrownBy(() -> useCase.execute(harvest, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data de início não pode ser superior à data de término.");

        verify(harvestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Período sobreposto a safra existente lança IllegalArgumentException")
    void givenOverlappingDates_whenRegister_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        Harvest existing = new Harvest(
                UUID.randomUUID(), "Safra Antiga",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );
        Harvest newHarvest = new Harvest(
                null, "Safra Nova",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 10, 31)
        );

        when(harvestRepository.existsByLabel("Safra Nova")).thenReturn(false);
        when(harvestRepository.findAll()).thenReturn(List.of(existing));

        assertThatThrownBy(() -> useCase.execute(newHarvest, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma safra cadastrada com período sobreposto");

        verify(harvestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rótulo duplicado lança HarvestAlreadyExistsException")
    void givenDuplicateLabel_whenRegister_thenThrowsHarvestAlreadyExistsException() {
        User admin = UserTestFactory.user().admin().build();
        Harvest harvest = new Harvest(
                null, "Safra Existente",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );

        when(harvestRepository.existsByLabel("Safra Existente")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(harvest, admin))
                .isInstanceOf(HarvestAlreadyExistsException.class);

        verify(harvestRepository, never()).save(any());
    }

    @Test
    @DisplayName("FARMER não tem permissão para cadastrar safra")
    void givenFarmer_whenRegister_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        Harvest harvest = new Harvest(
                null, "Safra",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );

        assertThatThrownBy(() -> useCase.execute(harvest, farmer))
                .isInstanceOf(AccessDeniedException.class);

        verify(harvestRepository, never()).save(any());
    }
}
