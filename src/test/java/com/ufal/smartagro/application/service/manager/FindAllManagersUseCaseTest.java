package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllManagersUseCaseTest {

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private FindAllManagersUseCase findAllManagersUseCase;

    @Test
    @DisplayName("Given ADMIN When findAll sem filtro Then consulta findAll e retorna gestores ativos")
    void givenAdmin_whenFindAllWithoutFilter_thenReturnsActiveManagers() {
        User admin = UserTestFactory.user().admin().build();
        Organization org1 = UserTestFactory.organization().build();
        Manager activeManager = UserTestFactory.managerProfile().organization(org1).build();
        Manager deletedManager = new Manager(
                UUID.randomUUID(), null, org1,
                LocalDateTime.now().minusDays(5), null, LocalDateTime.now().minusDays(1)
        );

        when(managerRepository.findAll()).thenReturn(List.of(activeManager, deletedManager));

        List<Manager> result = findAllManagersUseCase.findAll(null, admin);

        assertThat(result).containsExactly(activeManager);
        verify(managerRepository).findAll();
    }

    @Test
    @DisplayName("Given ADMIN When findAll com organizationId Then consulta findAllByOrganizationId no banco")
    void givenAdmin_whenFindAllWithOrgFilter_thenDelegatesToDatabaseQuery() {
        User admin = UserTestFactory.user().admin().build();
        Organization org1 = UserTestFactory.organization().build();
        Manager manager1 = UserTestFactory.managerProfile().organization(org1).build();

        when(managerRepository.findAllByOrganizationId(org1.getId())).thenReturn(List.of(manager1));

        List<Manager> result = findAllManagersUseCase.findAll(org1.getId(), admin);

        assertThat(result).containsExactly(manager1);
        verify(managerRepository).findAllByOrganizationId(org1.getId());
    }

    @Test
    @DisplayName("Given MANAGER When findAll Then AccessDeniedException")
    void givenManager_whenFindAll_thenThrowsAccessDeniedException() {
        User manager = UserTestFactory.user().manager().build();

        assertThatThrownBy(() -> findAllManagersUseCase.findAll(null, manager))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores podem listar gestores.");
    }
}
