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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindTechnicianByUserUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private FindTechnicianByUserUseCase findTechnicianByUserUseCase;

    @Test
    @DisplayName("Given TECHNICIAN User When findByUser Then retorna técnico com sucesso")
    void givenTechnicianUser_whenFindByUser_thenReturnsTechnician() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();

        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));

        Technician result = findTechnicianByUserUseCase.findByUser(techUser);

        assertThat(result).isEqualTo(technician);
    }

    @Test
    @DisplayName("Given não-TECHNICIAN User When findByUser Then AccessDeniedException")
    void givenNonTechnicianUser_whenFindByUser_thenThrowsAccessDeniedException() {
        User adminUser = UserTestFactory.user().admin().build();

        assertThatThrownBy(() -> findTechnicianByUserUseCase.findByUser(adminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado para visualizar dados de técnico.");
    }

    @Test
    @DisplayName("Given null User When findByUser Then AccessDeniedException")
    void givenNullUser_whenFindByUser_thenThrowsAccessDeniedException() {
        assertThatThrownBy(() -> findTechnicianByUserUseCase.findByUser(null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado para visualizar dados de técnico.");
    }

    @Test
    @DisplayName("Given técnico inexistente When findByUser Then EntityNotFoundException")
    void givenMissingTechnician_whenFindByUser_thenThrowsEntityNotFoundException() {
        User techUser = UserTestFactory.user().technician().build();

        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findTechnicianByUserUseCase.findByUser(techUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given técnico excluído (soft-delete) When findByUser Then EntityNotFoundException")
    void givenSoftDeletedTechnician_whenFindByUser_thenThrowsEntityNotFoundException() {
        User techUser = UserTestFactory.user().technician().build();
        Technician deletedTechnician = new Technician(
                UUID.randomUUID(), techUser, "CRBio-12345", "Agronomia",
                LocalDateTime.now().minusDays(10), null, LocalDateTime.now().minusDays(1)
        );

        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(deletedTechnician));

        assertThatThrownBy(() -> findTechnicianByUserUseCase.findByUser(techUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given userId válido When findByUserId Then retorna técnico")
    void givenValidUserId_whenFindByUserId_thenReturnsTechnician() {
        UUID userId = UUID.randomUUID();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(technicianRepository.findByUserId(userId)).thenReturn(Optional.of(technician));

        Technician result = findTechnicianByUserUseCase.findByUserId(userId);

        assertThat(result).isEqualTo(technician);
    }
}
