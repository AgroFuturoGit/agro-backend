package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private DeleteTechnicianUseCase deleteTechnicianUseCase;

    @Test
    @DisplayName("Given ADMIN When delete Then exclui com sucesso")
    void givenAdmin_whenDelete_thenDeletesSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));

        deleteTechnicianUseCase.delete(technician.getId(), admin);

        verify(technicianRepository).delete(technician.getId());
    }

    @Test
    @DisplayName("Given FARMER When delete Then AccessDeniedException")
    void givenFarmer_whenDelete_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> deleteTechnicianUseCase.delete(id, farmer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores podem excluir técnicos.");
    }

    @Test
    @DisplayName("Given null user When delete Then AccessDeniedException")
    void givenNullUser_whenDelete_thenThrowsAccessDeniedException() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> deleteTechnicianUseCase.delete(id, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores podem excluir técnicos.");
    }

    @Test
    @DisplayName("Given técnico inexistente When delete Then EntityNotFoundException")
    void givenMissingTechnician_whenDelete_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(technicianRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteTechnicianUseCase.delete(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given técnico já excluído (soft-delete) When delete Then EntityNotFoundException")
    void givenAlreadyDeletedTechnician_whenDelete_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();
        Technician deletedTechnician = new Technician(
                id, null, "CRBio-1", "Agro",
                LocalDateTime.now().minusDays(5), null, LocalDateTime.now().minusDays(1)
        );

        when(technicianRepository.findById(id)).thenReturn(Optional.of(deletedTechnician));

        assertThatThrownBy(() -> deleteTechnicianUseCase.delete(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }
}
