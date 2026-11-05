package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindManagerByIdUseCaseTest {

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private FindManagerByIdUseCase findManagerByIdUseCase;

    @Test
    @DisplayName("Given ADMIN When findById Then retorna gestor")
    void givenAdmin_whenFindById_thenReturnsManager() {
        User admin = UserTestFactory.user().admin().build();
        Manager manager = UserTestFactory.managerProfile().build();

        when(managerRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

        Manager result = findManagerByIdUseCase.findById(manager.getId(), admin);

        assertThat(result).isEqualTo(manager);
    }

    @Test
    @DisplayName("Given MANAGER da mesma organização When findById Then retorna gestor")
    void givenManagerSameOrg_whenFindById_thenReturnsManager() {
        Organization org = UserTestFactory.organization().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager self = UserTestFactory.managerProfile().user(managerUser).organization(org).build();
        Manager peer = UserTestFactory.managerProfile().organization(org).build();

        when(managerRepository.findById(peer.getId())).thenReturn(Optional.of(peer));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(self));

        Manager result = findManagerByIdUseCase.findById(peer.getId(), managerUser);

        assertThat(result).isEqualTo(peer);
    }

    @Test
    @DisplayName("Given MANAGER de outra organização When findById Then AccessDeniedException")
    void givenManagerOtherOrg_whenFindById_thenThrowsAccessDeniedException() {
        Organization org1 = UserTestFactory.organization().build();
        Organization org2 = UserTestFactory.organization().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager self = UserTestFactory.managerProfile().user(managerUser).organization(org1).build();
        Manager other = UserTestFactory.managerProfile().organization(org2).build();

        when(managerRepository.findById(other.getId())).thenReturn(Optional.of(other));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(self));

        assertThatThrownBy(() -> findManagerByIdUseCase.findById(other.getId(), managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O gestor só tem acesso aos gestores da sua própria organização.");
    }

    @Test
    @DisplayName("Given gestor inexistente When findById Then EntityNotFoundException")
    void givenMissingManager_whenFindById_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(managerRepository.findById(id)).thenReturn(Optional.empty());
        when(managerRepository.findByUserId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findManagerByIdUseCase.findById(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Gestor não encontrado.");
    }
}
