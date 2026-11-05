package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindUserByIdUseCase — autorização e isolamento multi-tenant (RBAC e prevenção de IDOR)")
class FindUserByIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private FindUserByIdUseCase useCase;

    private Organization org1;
    private Organization org2;
    private Community comm1;
    private User adminUser;
    private User managerUser;
    private Manager manager;
    private User techUser;
    private Technician technician;
    private User farmerUser;
    private Farmer farmer;

    @BeforeEach
    void setUp() {
        org1 = UserTestFactory.organization().name("Cooperativa 1").build();
        org2 = UserTestFactory.organization().name("Cooperativa 2").build();
        comm1 = UserTestFactory.community().name("Comunidade 1").organization(org1).build();

        adminUser = UserTestFactory.user().admin().build();

        managerUser = UserTestFactory.user().manager().build();
        manager = UserTestFactory.managerProfile().user(managerUser).organization(org1).build();

        techUser = UserTestFactory.user().technician().build();
        technician = UserTestFactory.technicianProfile().user(techUser).build();

        farmerUser = UserTestFactory.user().farmer().build();
        farmer = UserTestFactory.farmerProfile().user(farmerUser).community(comm1).build();
    }

    @Test
    @DisplayName("ADMIN pode consultar qualquer usuário pelo ID")
    void adminCanFindAnyUser() {
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        User result = useCase.findById(farmerUser.getId(), adminUser);

        assertThat(result).isEqualTo(farmerUser);
        verify(userRepository).findById(farmerUser.getId());
    }

    @Test
    @DisplayName("FARMER pode consultar seu próprio perfil de usuário")
    void farmerCanFindOwnProfile() {
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        User result = useCase.findById(farmerUser.getId(), farmerUser);

        assertThat(result).isEqualTo(farmerUser);
    }

    @Test
    @DisplayName("TECHNICIAN pode consultar seu próprio perfil de usuário")
    void technicianCanFindOwnProfile() {
        when(userRepository.findById(techUser.getId())).thenReturn(Optional.of(techUser));

        User result = useCase.findById(techUser.getId(), techUser);

        assertThat(result).isEqualTo(techUser);
    }

    @Test
    @DisplayName("FARMER tentando consultar outro usuário recebe AccessDeniedException (IDOR)")
    void farmerCannotFindOtherUserProfile() {
        when(userRepository.findById(techUser.getId())).thenReturn(Optional.of(techUser));

        assertThatThrownBy(() -> useCase.findById(techUser.getId(), farmerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Você só tem acesso aos dados da sua própria conta de usuário.");
    }

    @Test
    @DisplayName("TECHNICIAN tentando consultar outro usuário recebe AccessDeniedException (IDOR)")
    void technicianCannotFindOtherUserProfile() {
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        assertThatThrownBy(() -> useCase.findById(farmerUser.getId(), techUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Você só tem acesso aos dados da sua própria conta de usuário.");
    }

    @Test
    @DisplayName("MANAGER pode consultar seu próprio perfil")
    void managerCanFindOwnProfile() {
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        User result = useCase.findById(managerUser.getId(), managerUser);

        assertThat(result).isEqualTo(managerUser);
    }

    @Test
    @DisplayName("MANAGER pode consultar agricultor da sua organização")
    void managerCanFindFarmerFromOwnOrg() {
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(managerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.empty());
        when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));

        User result = useCase.findById(farmerUser.getId(), managerUser);

        assertThat(result).isEqualTo(farmerUser);
    }

    @Test
    @DisplayName("MANAGER pode consultar técnico da sua organização")
    void managerCanFindTechnicianFromOwnOrg() {
        when(userRepository.findById(techUser.getId())).thenReturn(Optional.of(techUser));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(managerRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());
        when(farmerRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));
        when(technicianRepository.existsByIdAndOrganizationId(technician.getId(), org1.getId())).thenReturn(true);

        User result = useCase.findById(techUser.getId(), managerUser);

        assertThat(result).isEqualTo(techUser);
    }

    @Test
    @DisplayName("MANAGER pode consultar técnico recém-criado por ele mesmo sem assistência ativa")
    void managerCanFindTechnicianCreatedByHimWithoutAssistance() {
        Technician createdTech = UserTestFactory.technicianProfile().user(techUser).createdBy(managerUser).build();

        when(userRepository.findById(techUser.getId())).thenReturn(Optional.of(techUser));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(managerRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());
        when(farmerRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(createdTech));

        User result = useCase.findById(techUser.getId(), managerUser);

        assertThat(result).isEqualTo(techUser);
    }

    @Test
    @DisplayName("MANAGER tentando consultar usuário de outra organização recebe AccessDeniedException")
    void managerCannotFindUserFromAnotherOrg() {
        User otherFarmerUser = UserTestFactory.user().farmer().build();
        Community otherComm = UserTestFactory.community().organization(org2).build();
        Farmer otherFarmer = UserTestFactory.farmerProfile().user(otherFarmerUser).community(otherComm).build();

        when(userRepository.findById(otherFarmerUser.getId())).thenReturn(Optional.of(otherFarmerUser));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(managerRepository.findByUserId(otherFarmerUser.getId())).thenReturn(Optional.empty());
        when(farmerRepository.findByUserId(otherFarmerUser.getId())).thenReturn(Optional.of(otherFarmer));

        assertThatThrownBy(() -> useCase.findById(otherFarmerUser.getId(), managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O gestor só tem acesso aos usuários da sua própria organização.");
    }

    @Test
    @DisplayName("Usuário inexistente lança UserNotFoundException")
    void missingUserThrowsUserNotFoundException() {
        UUID randomId = UUID.randomUUID();
        when(userRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.findById(randomId, adminUser))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Usuário logado nulo lança AccessDeniedException")
    void nullLoggedUserThrowsAccessDeniedException() {
        assertThatThrownBy(() -> useCase.findById(UUID.randomUUID(), null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Usuário não autenticado.");
    }
}
