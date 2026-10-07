package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveCommunityFromTechnicianUseCaseTest {

    @Mock
    private TechnicalAssistanceRepository assistanceRepository;
    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private RemoveCommunityFromTechnicianUseCase removeCommunityFromTechnicianUseCase;

    @Test
    @DisplayName("Given ADMIN When remove Then encerra assistência")
    void givenAdmin_whenRemove_thenEndsAssistance() {
        User admin = UserTestFactory.user().admin().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));
        when(assistanceRepository.save(any(TechnicalAssistance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TechnicalAssistance result = removeCommunityFromTechnicianUseCase.remove(assistance.getId(), admin);

        assertThat(result.getEndDate()).isNotNull();
        assertThat(result.getId()).isEqualTo(assistance.getId());
    }

    @Test
    @DisplayName("Given MANAGER When community de outra org Then AccessDeniedException")
    void givenManager_whenCommunityInAnotherOrg_thenAccessDenied() {
        Organization managerOrg = UserTestFactory.organization().name("Org Gestor").build();
        Organization otherOrg = UserTestFactory.organization().name("Org Alheia").build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(managerOrg).build();
        Community community = UserTestFactory.community().organization(otherOrg).build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().community(community).build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(assistance.getId(), managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O gestor só pode encerrar assistência de comunidades da sua organização.");

        verify(assistanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given FARMER When encerra assistência Then AccessDeniedException")
    void givenFarmer_whenRemoveAssistance_thenAccessDenied() {
        User farmerUser = UserTestFactory.user().farmer().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(assistance.getId(), farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado para encerrar assistência.");
    }

    @Test
    @DisplayName("Given TECHNICIAN When encerra assistência de outro técnico Then AccessDeniedException")
    void givenTechnician_whenRemoveOtherTechnicianAssistance_thenAccessDenied() {
        User techUser = UserTestFactory.user().technician().build();
        Technician otherTech = UserTestFactory.technicianProfile()
                .user(UserTestFactory.user().technician().email("outro.tech@smartagro.test").build())
                .build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().technician(otherTech).build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(assistance.getId(), techUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Técnico só pode encerrar sua própria assistência.");
    }

    @Test
    @DisplayName("Given assistência inexistente When remove Then EntityNotFoundException")
    void givenMissingAssistance_whenRemove_thenEntityNotFound() {
        User admin = UserTestFactory.user().admin().build();
        UUID missingId = UUID.randomUUID();
        when(assistanceRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(missingId, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Assistência não encontrada.");
    }

    @Test
    @DisplayName("Given assistência já encerrada When remove Then IllegalArgumentException")
    void givenAlreadyEndedAssistance_whenRemove_thenThrows() {
        User admin = UserTestFactory.user().admin().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().ended().build();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(assistance.getId(), admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assistência técnica já se encontra encerrada.");
    }

    @Test
    @DisplayName("Given technicianId divergente da assistência When remove Then EntityNotFoundException")
    void givenMismatchedTechnicianId_whenRemove_thenEntityNotFound() {
        User admin = UserTestFactory.user().admin().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().build();
        UUID differentTechnicianId = UUID.randomUUID();
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(differentTechnicianId, assistance.getId(), admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Assistência técnica não encontrada para o técnico informado.");
    }

    @Test
    @DisplayName("Given assistência excluída (soft-delete) When remove Then EntityNotFoundException")
    void givenSoftDeletedAssistance_whenRemove_thenEntityNotFound() {
        User admin = UserTestFactory.user().admin().build();
        TechnicalAssistance assistance = new TechnicalAssistance(
                UUID.randomUUID(), null, null, java.time.LocalDateTime.now().minusDays(5),
                null, java.time.LocalDateTime.now().minusDays(5), null, java.time.LocalDateTime.now().minusDays(1)
        );
        when(assistanceRepository.findById(assistance.getId())).thenReturn(Optional.of(assistance));

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(assistance.getId(), admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Assistência não encontrada.");
    }

    @Test
    @DisplayName("Given null user When remove Then AccessDeniedException")
    void givenNullUser_whenRemove_thenAccessDenied() {
        UUID assistanceId = UUID.randomUUID();

        assertThatThrownBy(() -> removeCommunityFromTechnicianUseCase.remove(assistanceId, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado para encerrar assistência.");
    }
}

