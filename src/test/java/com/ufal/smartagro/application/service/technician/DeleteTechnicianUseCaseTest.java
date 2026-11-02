package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;

    private DeleteTechnicianUseCase deleteTechnicianUseCase;

    @BeforeEach
    void setUp() {
        deleteTechnicianUseCase = new DeleteTechnicianUseCase(technicianRepository, userRepository, technicalAssistanceRepository);
    }

    @Test
    @DisplayName("Given ADMIN When delete Then encerra assistências ativas, exclui técnico e faz softDelete do User")
    void givenAdmin_whenDelete_thenEndsActiveAssistancesAndDeletesSuccessfully() {
        User technicianUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(technicianUser).build();
        User admin = UserTestFactory.user().admin().build();

        TechnicalAssistance activeAssistance = new TechnicalAssistance(
                UUID.randomUUID(), technician, mock(Community.class),
                LocalDateTime.now().minusMonths(1), null,
                LocalDateTime.now().minusMonths(1), null, null
        );

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(technicalAssistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(activeAssistance));

        deleteTechnicianUseCase.delete(technician.getId(), admin);

        ArgumentCaptor<TechnicalAssistance> captor = ArgumentCaptor.forClass(TechnicalAssistance.class);
        verify(technicalAssistanceRepository).save(captor.capture());
        assertThat(captor.getValue().getEndDate()).isNotNull();

        verify(technicianRepository).delete(technician.getId());
        verify(userRepository).softDelete(technicianUser.getId());
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
