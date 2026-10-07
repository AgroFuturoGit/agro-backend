package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianUpdateDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTechnicianUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private UpdateTechnicianUseCase updateTechnicianUseCase;

    @Test
    @DisplayName("Given ADMIN When update Then atualiza com sucesso")
    void givenAdmin_whenUpdate_thenUpdatesSuccessfully() {
        User admin = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-Novo", "Solo");

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(technicianRepository.save(any(Technician.class))).thenAnswer(inv -> inv.getArgument(0));

        Technician result = updateTechnicianUseCase.update(technician.getId(), dto, admin);

        assertThat(result.getProfessionalId()).isEqualTo("CRBio-Novo");
        assertThat(result.getSpecialty()).isEqualTo("Solo");
    }

    @Test
    @DisplayName("Given TECHNICIAN When atualiza seu próprio perfil Then atualiza com sucesso")
    void givenTechnician_whenUpdatesOwnProfile_thenUpdatesSuccessfully() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-Novo", null);

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(technicianRepository.save(any(Technician.class))).thenAnswer(inv -> inv.getArgument(0));

        Technician result = updateTechnicianUseCase.update(technician.getId(), dto, techUser);

        assertThat(result.getProfessionalId()).isEqualTo("CRBio-Novo");
        assertThat(result.getSpecialty()).isEqualTo(technician.getSpecialty());
    }

    @Test
    @DisplayName("Given TECHNICIAN When tenta atualizar perfil de outro Then AccessDeniedException")
    void givenTechnician_whenUpdatesOtherProfile_thenThrowsAccessDeniedException() {
        User techUser = UserTestFactory.user().technician().build();
        User otherTechUser = UserTestFactory.user().technician().email("outro@smartagro.test").build();
        Technician otherTechnician = UserTestFactory.technicianProfile().user(otherTechUser).build();
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-Novo", "Solo");

        when(technicianRepository.findById(otherTechnician.getId())).thenReturn(Optional.of(otherTechnician));

        assertThatThrownBy(() -> updateTechnicianUseCase.update(otherTechnician.getId(), dto, techUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O técnico só pode atualizar seu próprio perfil.");
    }

    @Test
    @DisplayName("Given FARMER When update Then AccessDeniedException")
    void givenFarmer_whenUpdate_thenThrowsAccessDeniedException() {
        User farmer = UserTestFactory.user().farmer().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-Novo", "Solo");

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));

        assertThatThrownBy(() -> updateTechnicianUseCase.update(technician.getId(), dto, farmer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores ou o próprio técnico podem atualizar este perfil.");
    }

    @Test
    @DisplayName("Given null user When update Then AccessDeniedException")
    void givenNullUser_whenUpdate_thenThrowsAccessDeniedException() {
        UUID id = UUID.randomUUID();
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-Novo", "Solo");

        assertThatThrownBy(() -> updateTechnicianUseCase.update(id, dto, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores ou o próprio técnico podem atualizar este perfil.");
    }

    @Test
    @DisplayName("Given técnico inexistente When update Then EntityNotFoundException")
    void givenMissingTechnician_whenUpdate_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-Novo", "Solo");

        when(technicianRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateTechnicianUseCase.update(id, dto, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given técnico excluído (soft-delete) When update Then EntityNotFoundException")
    void givenSoftDeletedTechnician_whenUpdate_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();
        Technician deletedTechnician = new Technician(
                id, null, "CRBio-1", "Agro",
                LocalDateTime.now().minusDays(5), null, LocalDateTime.now().minusDays(1)
        );
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-Novo", "Solo");

        when(technicianRepository.findById(id)).thenReturn(Optional.of(deletedTechnician));

        assertThatThrownBy(() -> updateTechnicianUseCase.update(id, dto, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }
}
