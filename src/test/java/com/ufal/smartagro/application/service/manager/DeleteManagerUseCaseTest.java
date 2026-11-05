package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteManagerUseCaseTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private DeleteManagerUseCase deleteManagerUseCase;

    @BeforeEach
    void setUp() {
        deleteManagerUseCase = new DeleteManagerUseCase(
                managerRepository,
                userRepository,
                auditLogService
        );
    }

    @Test
    @DisplayName("Given ADMIN e managerId válido When delete Then faz softDelete do Manager e do User e gera auditoria")
    void givenAdminAndManagerId_whenDelete_thenDeletesManagerAndUserSuccessfully() {
        Organization org = UserTestFactory.organization().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org).build();
        User admin = UserTestFactory.user().admin().build();

        when(managerRepository.findById(manager.getId())).thenReturn(Optional.of(manager));

        deleteManagerUseCase.delete(manager.getId(), admin);

        verify(managerRepository).delete(manager.getId());
        verify(userRepository).softDelete(managerUser.getId());
        verify(auditLogService).record(eq(admin), eq(AuditAction.DELETE), eq("managers"), eq(manager.getId()), any());
    }

    @Test
    @DisplayName("Given ADMIN e userId vinculado When delete Then resolve Manager pelo userId e faz softDelete")
    void givenAdminAndUserId_whenDelete_thenResolvesByUserIdAndDeletesSuccessfully() {
        Organization org = UserTestFactory.organization().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).organization(org).build();
        User admin = UserTestFactory.user().admin().build();

        when(managerRepository.findById(managerUser.getId())).thenReturn(Optional.empty());
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        deleteManagerUseCase.delete(managerUser.getId(), admin);

        verify(managerRepository).delete(manager.getId());
        verify(userRepository).softDelete(managerUser.getId());
        verify(auditLogService).record(eq(admin), eq(AuditAction.DELETE), eq("managers"), eq(manager.getId()), any());
    }

    @Test
    @DisplayName("Given MANAGER When delete Then AccessDeniedException")
    void givenManager_whenDelete_thenThrowsAccessDeniedException() {
        User managerUser = UserTestFactory.user().manager().build();
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> deleteManagerUseCase.delete(id, managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores podem excluir gestores.");

        verifyNoInteractions(managerRepository, userRepository, auditLogService);
    }

    @Test
    @DisplayName("Given null user When delete Then AccessDeniedException")
    void givenNullUser_whenDelete_thenThrowsAccessDeniedException() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> deleteManagerUseCase.delete(id, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores podem excluir gestores.");

        verifyNoInteractions(managerRepository, userRepository, auditLogService);
    }

    @Test
    @DisplayName("Given gestor inexistente When delete Then EntityNotFoundException")
    void givenMissingManager_whenDelete_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();

        when(managerRepository.findById(id)).thenReturn(Optional.empty());
        when(managerRepository.findByUserId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteManagerUseCase.delete(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Gestor não encontrado.");
    }

    @Test
    @DisplayName("Given gestor já excluído (soft-delete) When delete Then EntityNotFoundException")
    void givenAlreadyDeletedManager_whenDelete_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID id = UUID.randomUUID();
        Manager deletedManager = new Manager(
                id, null, null,
                LocalDateTime.now().minusDays(5), null, LocalDateTime.now().minusDays(1)
        );

        when(managerRepository.findById(id)).thenReturn(Optional.of(deletedManager));
        when(managerRepository.findByUserId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteManagerUseCase.delete(id, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Gestor não encontrado.");
    }
}
