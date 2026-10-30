package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private FarmerRepository farmerRepository;

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
    @DisplayName("Given MANAGER When findAll Then busca agricultores por organização sem carregar toda a tabela")
    void givenManager_whenFindAll_thenQueriesFarmersByOrganization() {
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();

        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(managerRepository.findAll()).thenReturn(List.of(manager));
        when(farmerRepository.findAllByOrganizationId(organization.getId())).thenReturn(List.of(farmer));
        when(userRepository.findAll()).thenReturn(List.of(managerUser, farmerUser));

        List<User> result = useCase.findAll(managerUser);

        assertThat(result).hasSize(2).contains(managerUser, farmerUser);
        verify(farmerRepository, never()).findAll();
        verify(farmerRepository).findAllByOrganizationId(organization.getId());
    }

    @Test
    @DisplayName("Given TECHNICIAN When findAll Then AccessDeniedException")
    void givenTechnician_whenFindAll_thenAccessDenied() {
        User tech = UserTestFactory.user().technician().build();

        assertThatThrownBy(() -> useCase.findAll(tech))
                .isInstanceOf(AccessDeniedException.class);
    }
}
