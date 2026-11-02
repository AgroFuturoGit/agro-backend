package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindCommunityUseCaseTest {

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @Mock
    private TechnicalAssistanceRepository technicalAssistanceRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @InjectMocks
    private FindAllCommunitiesUseCase findAllCommunitiesUseCase;

    @InjectMocks
    private FindCommunityByIdUseCase findCommunityByIdUseCase;

    private Organization org1;
    private Organization org2;
    private Community comm1;
    private Community comm2;
    private User adminUser;
    private User managerUser;
    private User techUser;
    private User farmerUser;
    private Manager manager;
    private Technician technician;
    private Farmer farmer;

    @BeforeEach
    void setUp() {
        org1 = UserTestFactory.organization().name("Org 1").build();
        org2 = UserTestFactory.organization().name("Org 2").build();

        comm1 = UserTestFactory.community().name("Comm 1").organization(org1).build();
        comm2 = UserTestFactory.community().name("Comm 2").organization(org2).build();

        adminUser = UserTestFactory.user().role(Role.ADMIN).build();

        managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        manager = UserTestFactory.managerProfile().user(managerUser).organization(org1).build();

        techUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        technician = UserTestFactory.technicianProfile().user(techUser).build();

        farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        farmer = UserTestFactory.farmerProfile().user(farmerUser).community(comm1).build();
    }

    @Nested
    @DisplayName("FindAllCommunitiesUseCase Tests")
    class FindAllCommunitiesTests {

        @Test
        @DisplayName("ADMIN sem orgId lista todas as comunidades")
        void adminWithoutOrgIdReturnsAll() {
            when(communityRepository.findAll()).thenReturn(List.of(comm1, comm2));

            List<Community> result = findAllCommunitiesUseCase.findAll(null, adminUser);

            assertThat(result).containsExactly(comm1, comm2);
            verify(communityRepository).findAll();
        }

        @Test
        @DisplayName("ADMIN com orgId lista comunidades da organização especificada")
        void adminWithOrgIdReturnsFiltered() {
            when(communityRepository.findAllByOrganizationId(org1.getId())).thenReturn(List.of(comm1));

            List<Community> result = findAllCommunitiesUseCase.findAll(org1.getId(), adminUser);

            assertThat(result).containsExactly(comm1);
            verify(communityRepository).findAllByOrganizationId(org1.getId());
        }

        @Test
        @DisplayName("MANAGER lista apenas as comunidades de sua organização")
        void managerReturnsOwnOrgCommunities() {
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
            when(communityRepository.findAllByOrganizationId(org1.getId())).thenReturn(List.of(comm1));

            List<Community> result = findAllCommunitiesUseCase.findAll(null, managerUser);

            assertThat(result).containsExactly(comm1);
            verify(communityRepository).findAllByOrganizationId(org1.getId());
        }

        @Test
        @DisplayName("MANAGER com orgId de outra organização lança AccessDeniedException")
        void managerWithOtherOrgIdThrowsAccessDenied() {
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> findAllCommunitiesUseCase.findAll(org2.getId(), managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("O gestor só tem acesso às comunidades da sua própria organização.");
        }

        @Test
        @DisplayName("TECHNICIAN lista apenas as comunidades sob assistência técnica ativa")
        void technicianReturnsAssistedCommunities() {
            TechnicalAssistance activeTa = UserTestFactory.technicalAssistance()
                    .technician(technician)
                    .community(comm1)
                    .build();

            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(activeTa));

            List<Community> result = findAllCommunitiesUseCase.findAll(null, techUser);

            assertThat(result).containsExactly(comm1);
        }

        @Test
        @DisplayName("TECHNICIAN com filtro por orgId lista apenas comunidades assistidas dessa organização")
        void technicianWithOrgIdFiltersAssistedCommunities() {
            TechnicalAssistance activeTa1 = UserTestFactory.technicalAssistance()
                    .technician(technician)
                    .community(comm1)
                    .build();
            TechnicalAssistance activeTa2 = UserTestFactory.technicalAssistance()
                    .technician(technician)
                    .community(comm2)
                    .build();

            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findByTechnicianId(technician.getId())).thenReturn(List.of(activeTa1, activeTa2));

            List<Community> result = findAllCommunitiesUseCase.findAll(org1.getId(), techUser);

            assertThat(result).containsExactly(comm1);
        }

        @Test
        @DisplayName("FARMER lista apenas a sua própria comunidade")
        void farmerReturnsOwnCommunity() {
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));

            List<Community> result = findAllCommunitiesUseCase.findAll(null, farmerUser);

            assertThat(result).containsExactly(comm1);
        }

        @Test
        @DisplayName("FARMER com orgId correspondente à sua comunidade tem sucesso")
        void farmerWithMatchingOrgIdReturnsOwnCommunity() {
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));

            List<Community> result = findAllCommunitiesUseCase.findAll(org1.getId(), farmerUser);

            assertThat(result).containsExactly(comm1);
        }

        @Test
        @DisplayName("FARMER com orgId divergente de sua comunidade lança AccessDeniedException")
        void farmerWithOtherOrgIdThrowsAccessDenied() {
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));

            assertThatThrownBy(() -> findAllCommunitiesUseCase.findAll(org2.getId(), farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("O agricultor só tem acesso à sua própria comunidade.");
        }

        @Test
        @DisplayName("FARMER sem comunidade vinculada retorna lista vazia")
        void farmerWithoutCommunityReturnsEmptyList() {
            Farmer unlinkedFarmer = UserTestFactory.farmerProfile().user(farmerUser).community(null).build();
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(unlinkedFarmer));

            List<Community> result = findAllCommunitiesUseCase.findAll(null, farmerUser);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindCommunityByIdUseCase Tests")
    class FindCommunityByIdTests {

        @Test
        @DisplayName("ADMIN consulta qualquer comunidade com sucesso")
        void adminFindsAnyCommunity() {
            when(communityRepository.findById(comm1.getId())).thenReturn(Optional.of(comm1));

            Community result = findCommunityByIdUseCase.findById(comm1.getId(), adminUser);

            assertThat(result).isEqualTo(comm1);
        }

        @Test
        @DisplayName("MANAGER consulta comunidade da sua organização com sucesso")
        void managerFindsOwnOrgCommunity() {
            when(communityRepository.findById(comm1.getId())).thenReturn(Optional.of(comm1));
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            Community result = findCommunityByIdUseCase.findById(comm1.getId(), managerUser);

            assertThat(result).isEqualTo(comm1);
        }

        @Test
        @DisplayName("MANAGER ao consultar comunidade de outra organização lança AccessDeniedException")
        void managerFindsOtherOrgCommunityThrowsAccessDenied() {
            when(communityRepository.findById(comm2.getId())).thenReturn(Optional.of(comm2));
            when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

            assertThatThrownBy(() -> findCommunityByIdUseCase.findById(comm2.getId(), managerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("O gestor só tem acesso às comunidades da sua própria organização.");
        }

        @Test
        @DisplayName("TECHNICIAN consulta comunidade assistida com sucesso")
        void technicianFindsAssistedCommunity() {
            when(communityRepository.findById(comm1.getId())).thenReturn(Optional.of(comm1));
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), comm1.getId()))
                    .thenReturn(Optional.of(UserTestFactory.technicalAssistance().build()));

            Community result = findCommunityByIdUseCase.findById(comm1.getId(), techUser);

            assertThat(result).isEqualTo(comm1);
        }

        @Test
        @DisplayName("TECHNICIAN ao consultar comunidade não assistida lança AccessDeniedException")
        void technicianFindsUnassistedCommunityThrowsAccessDenied() {
            when(communityRepository.findById(comm2.getId())).thenReturn(Optional.of(comm2));
            when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
            when(technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), comm2.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> findCommunityByIdUseCase.findById(comm2.getId(), techUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("O técnico só tem acesso às comunidades sob sua assistência técnica ativa.");
        }

        @Test
        @DisplayName("FARMER consulta sua própria comunidade com sucesso")
        void farmerFindsOwnCommunity() {
            when(communityRepository.findById(comm1.getId())).thenReturn(Optional.of(comm1));
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));

            Community result = findCommunityByIdUseCase.findById(comm1.getId(), farmerUser);

            assertThat(result).isEqualTo(comm1);
        }

        @Test
        @DisplayName("FARMER ao consultar comunidade alheia lança AccessDeniedException")
        void farmerFindsForeignCommunityThrowsAccessDenied() {
            when(communityRepository.findById(comm2.getId())).thenReturn(Optional.of(comm2));
            when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));

            assertThatThrownBy(() -> findCommunityByIdUseCase.findById(comm2.getId(), farmerUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("O agricultor só tem acesso à sua própria comunidade.");
        }

        @Test
        @DisplayName("Comunidade inexistente lança EntityNotFoundException")
        void notFoundThrowsEntityNotFoundException() {
            UUID randomId = UUID.randomUUID();
            when(communityRepository.findById(randomId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> findCommunityByIdUseCase.findById(randomId, adminUser))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Comunidade não encontrada.");
        }
    }
}
