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
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAssignedCommunitiesUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private TechnicalAssistanceRepository assistanceRepository;
    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private GetAssignedCommunitiesUseCase getAssignedCommunitiesUseCase;

    @Test
    @DisplayName("Given ADMIN When getAssignedCommunities Then retorna todas comunidades ativas")
    void givenAdmin_whenGetAssignedCommunities_thenReturnsActive() {
        User admin = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        Community comm1 = UserTestFactory.community().name("Comunidade 1").build();
        Community comm2 = UserTestFactory.community().name("Comunidade 2").build();

        TechnicalAssistance active = UserTestFactory.technicalAssistance().technician(technician).community(comm1).build();
        TechnicalAssistance ended = UserTestFactory.technicalAssistance().technician(technician).community(comm2).ended().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(active, ended));

        List<Community> result = getAssignedCommunitiesUseCase.getAssignedCommunities(technician.getId(), admin);

        assertThat(result).containsExactly(comm1);
    }

    @Test
    @DisplayName("Given TECHNICIAN When busca suas próprias comunidades Then retorna com sucesso")
    void givenTechnician_whenOwnCommunities_thenReturnsSuccessfully() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community community = UserTestFactory.community().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().technician(technician).community(community).build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(assistance));

        List<Community> result = getAssignedCommunitiesUseCase.getAssignedCommunities(technician.getId(), techUser);

        assertThat(result).containsExactly(community);
    }

    @Test
    @DisplayName("Given TECHNICIAN When busca comunidades de outro técnico Then AccessDeniedException")
    void givenTechnician_whenOtherTechnician_thenAccessDenied() {
        User techUser = UserTestFactory.user().technician().build();
        Technician otherTech = UserTestFactory.technicianProfile()
                .user(UserTestFactory.user().technician().email("outro.tech@smartagro.test").build())
                .build();

        when(technicianRepository.findById(otherTech.getId())).thenReturn(Optional.of(otherTech));

        assertThatThrownBy(() -> getAssignedCommunitiesUseCase.getAssignedCommunities(otherTech.getId(), techUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Técnico só pode visualizar suas próprias comunidades.");
    }

    @Test
    @DisplayName("Given MANAGER When busca comunidades Then filtra pela sua organização")
    void givenManager_whenGetAssignedCommunities_thenFiltersByOrg() {
        Organization org1 = UserTestFactory.organization().name("Org 1").build();
        Organization org2 = UserTestFactory.organization().name("Org 2").build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org1).build();

        Technician technician = UserTestFactory.technicianProfile().build();
        Community comm1 = UserTestFactory.community().name("Comm 1").organization(org1).build();
        Community comm2 = UserTestFactory.community().name("Comm 2").organization(org2).build();

        TechnicalAssistance ta1 = UserTestFactory.technicalAssistance().technician(technician).community(comm1).build();
        TechnicalAssistance ta2 = UserTestFactory.technicalAssistance().technician(technician).community(comm2).build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(ta1, ta2));

        List<Community> result = getAssignedCommunitiesUseCase.getAssignedCommunities(technician.getId(), managerUser);

        assertThat(result).containsExactly(comm1);
    }

    @Test
    @DisplayName("Given técnico inexistente When busca Then EntityNotFoundException")
    void givenMissingTechnician_whenGet_thenEntityNotFound() {
        User admin = UserTestFactory.user().admin().build();
        UUID missingId = UUID.randomUUID();
        when(technicianRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAssignedCommunitiesUseCase.getAssignedCommunities(missingId, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given TECHNICIAN User When getAssignedCommunities(loggedUser) Then retorna comunidades com sucesso")
    void givenTechnicianUser_whenGetAssignedCommunitiesForUser_thenReturnsSuccessfully() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community community = UserTestFactory.community().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().technician(technician).community(community).build();

        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(assistance));

        List<Community> result = getAssignedCommunitiesUseCase.getAssignedCommunities(techUser);

        assertThat(result).containsExactly(community);
    }

    @Test
    @DisplayName("Given não-TECHNICIAN User When getAssignedCommunities(loggedUser) Then AccessDeniedException")
    void givenNonTechnicianUser_whenGetAssignedCommunitiesForUser_thenAccessDenied() {
        User adminUser = UserTestFactory.user().admin().build();

        assertThatThrownBy(() -> getAssignedCommunitiesUseCase.getAssignedCommunities(adminUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado para visualizar comunidades atribuídas.");
    }

    @Test
    @DisplayName("Given TECHNICIAN sem registro When getAssignedCommunities(loggedUser) Then EntityNotFoundException")
    void givenTechnicianUserNotFound_whenGetAssignedCommunitiesForUser_thenEntityNotFound() {
        User techUser = UserTestFactory.user().technician().build();
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAssignedCommunitiesUseCase.getAssignedCommunities(techUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given TECHNICIAN excluído (soft-delete) When getAssignedCommunities(loggedUser) Then EntityNotFoundException")
    void givenSoftDeletedTechnicianUser_whenGetAssignedCommunitiesForUser_thenEntityNotFound() {
        User techUser = UserTestFactory.user().technician().build();
        Technician deletedTechnician = new Technician(
                UUID.randomUUID(), techUser, "CRBio-1", "Agro",
                java.time.LocalDateTime.now().minusDays(5), null, java.time.LocalDateTime.now().minusDays(1)
        );
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(deletedTechnician));

        assertThatThrownBy(() -> getAssignedCommunitiesUseCase.getAssignedCommunities(techUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }

    @Test
    @DisplayName("Given null user When getAssignedCommunities Then AccessDeniedException")
    void givenNullUser_whenGetAssignedCommunities_thenAccessDenied() {
        assertThatThrownBy(() -> getAssignedCommunitiesUseCase.getAssignedCommunities(null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado para visualizar comunidades atribuídas.");

        assertThatThrownBy(() -> getAssignedCommunitiesUseCase.getAssignedCommunities(UUID.randomUUID(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado para visualizar comunidades atribuídas.");
    }
}
