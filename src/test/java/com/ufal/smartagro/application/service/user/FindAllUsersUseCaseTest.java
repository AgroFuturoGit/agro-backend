package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private FindAllUsersUseCase useCase;

    private Organization organization;
    private User managerUser;
    private Manager manager;

    @BeforeEach
    void setUp() {
        organization = UserTestFactory.organization().name("Org Teste").build();
        managerUser = UserTestFactory.user().manager().build();
        manager = UserTestFactory.managerProfile().user(managerUser).organization(organization).build();
    }

    @Test
    @DisplayName("Given MANAGER When findAll Then busca agricultores e técnicos da organização sem carregar tabela toda")
    void givenManager_whenFindAll_thenQueriesFarmersAndTechniciansByOrganization() {
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();

        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();

        User externalUser = UserTestFactory.user().farmer().build();

        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(managerRepository.findAll()).thenReturn(List.of(manager));
        when(farmerRepository.findAllByOrganizationId(organization.getId())).thenReturn(List.of(farmer));
        when(technicianRepository.findAllByOrganizationId(organization.getId())).thenReturn(List.of(technician));
        when(technicianRepository.findAll()).thenReturn(List.of(technician));
        when(userRepository.findAll()).thenReturn(List.of(managerUser, farmerUser, techUser, externalUser));

        List<User> result = useCase.findAll(managerUser);

        assertThat(result).hasSize(3).containsExactly(managerUser, farmerUser, techUser);
        verify(farmerRepository, never()).findAll();
        verify(farmerRepository).findAllByOrganizationId(organization.getId());
        verify(technicianRepository).findAllByOrganizationId(organization.getId());
    }

    @Test
    @DisplayName("Given MANAGER When findAll Then inclui usuário de técnico recém-criado pelo gestor ainda sem assistência")
    void givenManager_whenFindAll_thenIncludesCreatedTechnicianUser() {
        User createdTechUser = UserTestFactory.user().technician().build();
        Technician createdTechnician = UserTestFactory.technicianProfile()
                .user(createdTechUser)
                .createdBy(managerUser)
                .build();

        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(managerRepository.findAll()).thenReturn(List.of(manager));
        when(farmerRepository.findAllByOrganizationId(organization.getId())).thenReturn(List.of());
        when(technicianRepository.findAllByOrganizationId(organization.getId())).thenReturn(List.of());
        when(technicianRepository.findAll()).thenReturn(List.of(createdTechnician));
        when(userRepository.findAll()).thenReturn(List.of(managerUser, createdTechUser));

        List<User> result = useCase.findAll(managerUser);

        assertThat(result).containsExactly(managerUser, createdTechUser);
    }

    @Test
    @DisplayName("Given ADMIN When findAll Then retorna todos os usuários")
    void givenAdmin_whenFindAll_thenReturnsAllUsers() {
        User admin = UserTestFactory.user().admin().build();
        when(userRepository.findAll()).thenReturn(List.of(admin, managerUser));

        List<User> result = useCase.findAll(admin);

        assertThat(result).hasSize(2).contains(admin, managerUser);
        verifyNoInteractions(managerRepository, farmerRepository, technicianRepository);
    }

    @Test
    @DisplayName("Given TECHNICIAN When findAll Then AccessDeniedException")
    void givenTechnician_whenFindAll_thenAccessDenied() {
        User tech = UserTestFactory.user().technician().build();

        assertThatThrownBy(() -> useCase.findAll(tech))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Given FARMER When findAll Then AccessDeniedException")
    void givenFarmer_whenFindAll_thenAccessDenied() {
        User farmer = UserTestFactory.user().farmer().build();

        assertThatThrownBy(() -> useCase.findAll(farmer))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Given null user When findAll Then AccessDeniedException")
    void givenNullUser_whenFindAll_thenAccessDenied() {
        assertThatThrownBy(() -> useCase.findAll(null))
                .isInstanceOf(AccessDeniedException.class);
    }
}
