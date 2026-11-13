package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.application.service.technician.GetAssignedAssistancesUseCase.AssignedAssistanceItem;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAssignedAssistancesUseCaseTest {

    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private TechnicalAssistanceRepository assistanceRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private FarmerRepository farmerRepository;

    @InjectMocks
    private GetAssignedAssistancesUseCase getAssignedAssistancesUseCase;

    @Test
    @DisplayName("Given ADMIN When getAssignedAssistances Then retorna assistências ativas com contagem de agricultores")
    void givenAdmin_whenGetAssignedAssistances_thenReturnsActiveWithFarmerCount() {
        User admin = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        Community comm1 = UserTestFactory.community().name("Comunidade 1").build();
        Community comm2 = UserTestFactory.community().name("Comunidade 2").build();

        TechnicalAssistance active = UserTestFactory.technicalAssistance().technician(technician).community(comm1).build();
        TechnicalAssistance ended = UserTestFactory.technicalAssistance().technician(technician).community(comm2).ended().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(active, ended));
        when(farmerRepository.countByCommunityId(comm1.getId())).thenReturn(12L);

        List<AssignedAssistanceItem> result = getAssignedAssistancesUseCase.getAssignedAssistances(technician.getId(), admin);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).assistance()).isEqualTo(active);
        assertThat(result.get(0).activeFarmersCount()).isEqualTo(12L);
    }

    @Test
    @DisplayName("Given TECHNICIAN When getAssignedAssistances por usuário logado Then retorna suas assistências")
    void givenTechnician_whenGetOwnAssistances_thenReturnsSuccessfully() {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community community = UserTestFactory.community().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance().technician(technician).community(community).build();

        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(assistance));
        when(farmerRepository.countByCommunityId(community.getId())).thenReturn(5L);

        List<AssignedAssistanceItem> result = getAssignedAssistancesUseCase.getAssignedAssistances(techUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).assistance()).isEqualTo(assistance);
        assertThat(result.get(0).activeFarmersCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Given TECHNICIAN When consulta assistências de outro técnico Then AccessDeniedException")
    void givenTechnician_whenOtherTechnician_thenThrowsAccessDenied() {
        User techUser = UserTestFactory.user().technician().build();
        Technician otherTech = UserTestFactory.technicianProfile()
                .user(UserTestFactory.user().technician().email("outro.tech@smartagro.test").build())
                .build();

        when(technicianRepository.findById(otherTech.getId())).thenReturn(Optional.of(otherTech));

        assertThatThrownBy(() -> getAssignedAssistancesUseCase.getAssignedAssistances(otherTech.getId(), techUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Técnico só pode visualizar suas próprias assistências.");
    }

    @Test
    @DisplayName("Given MANAGER When consulta assistências Then filtra pela sua organização")
    void givenManager_whenGetAssignedAssistances_thenFiltersByOrganization() {
        Organization org1 = UserTestFactory.organization().name("Org 1").build();
        Organization org2 = UserTestFactory.organization().name("Org 2").build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org1).build();

        Technician technician = UserTestFactory.technicianProfile().build();
        Community commOrg1 = UserTestFactory.community().organization(org1).name("Comm Org 1").build();
        Community commOrg2 = UserTestFactory.community().organization(org2).name("Comm Org 2").build();

        TechnicalAssistance assistance1 = UserTestFactory.technicalAssistance().technician(technician).community(commOrg1).build();
        TechnicalAssistance assistance2 = UserTestFactory.technicalAssistance().technician(technician).community(commOrg2).build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(assistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(assistance1, assistance2));
        when(farmerRepository.countByCommunityId(commOrg1.getId())).thenReturn(8L);

        List<AssignedAssistanceItem> result = getAssignedAssistancesUseCase.getAssignedAssistances(technician.getId(), managerUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).assistance()).isEqualTo(assistance1);
        assertThat(result.get(0).activeFarmersCount()).isEqualTo(8L);
    }

    @Test
    @DisplayName("Given FARMER When consulta assistências Then AccessDeniedException")
    void givenFarmer_whenGetAssignedAssistances_thenThrowsAccessDenied() {
        User farmer = UserTestFactory.user().farmer().build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(technicianRepository.findById(technician.getId())).thenReturn(Optional.of(technician));

        assertThatThrownBy(() -> getAssignedAssistancesUseCase.getAssignedAssistances(technician.getId(), farmer))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Given técnico inexistente When getAssignedAssistances Then EntityNotFoundException")
    void givenMissingTechnician_whenGetAssignedAssistances_thenThrowsEntityNotFound() {
        User admin = UserTestFactory.user().admin().build();
        var missingId = java.util.UUID.randomUUID();

        when(technicianRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getAssignedAssistancesUseCase.getAssignedAssistances(missingId, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Técnico não encontrado.");
    }
}
