package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.AdminUserUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserUpdateUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminUserUpdateUseCase adminUserUpdateUseCase;

    @Test
    @DisplayName("Given ADMIN When update mantendo mesmo papel Then muda nome e preserva email, CPF e senha e grava auditoria")
    void givenAdmin_whenUpdate_thenKeepsEmailCpfAndPassword() {
        User admin = UserTestFactory.user().admin().build();
        User target = UserTestFactory.user()
                .technician()
                .email("tech@smartagro.test")
                .cpf(UserTestFactory.VALID_CPF)
                .password(UserTestFactory.ENCODED_PASSWORD)
                .build();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO(
                "Nome Tecnico Editado",
                Role.TECHNICIAN
        );
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User response = adminUserUpdateUseCase.update(target.getId(), dto, admin);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("tech@smartagro.test");
        assertThat(saved.getCpf()).isEqualTo(UserTestFactory.VALID_CPF);
        assertThat(saved.getPassword()).isEqualTo(UserTestFactory.ENCODED_PASSWORD);
        assertThat(saved.getFullName()).isEqualTo("Nome Tecnico Editado");
        assertThat(saved.getRole()).isEqualTo(Role.TECHNICIAN);
        assertThat(response.getEmail()).isEqualTo(target.getEmail());
        assertThat(response.getCpf()).isEqualTo(target.getCpf());
        verify(auditLogService).record(eq(admin), eq(AuditAction.UPDATE), eq("user"), eq(target.getId()), any());
    }

    @ParameterizedTest(name = "Given ADMIN When altera papel para {0} Then IllegalArgumentException (API-08)")
    @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
    void givenAdmin_whenPromoteToSpecializedRole_thenThrowsIllegalArgumentException(Role forbiddenRole) {
        User admin = UserTestFactory.user().admin().build();
        User target = UserTestFactory.user().admin().build();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO("Nome Alvo", forbiddenRole);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminUserUpdateUseCase.update(target.getId(), dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Para criar ou promover gestores, produtores e técnicos, utilize as rotas específicas de perfil.");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }

    @ParameterizedTest(name = "Given ADMIN When altera papel de usuário que já é {0} Then IllegalArgumentException (API-08)")
    @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
    void givenAdmin_whenChangeRoleOfSpecializedUser_thenThrowsIllegalArgumentException(Role currentSpecializedRole) {
        User admin = UserTestFactory.user().admin().build();
        User target = UserTestFactory.user().role(currentSpecializedRole).build();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO("Nome Alvo", Role.ADMIN);
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminUserUpdateUseCase.update(target.getId(), dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é permitido alterar o papel de um usuário que já possui perfil de gestor, produtor ou técnico vinculado.");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    @DisplayName("Given ADMIN When tenta rebaixar o próprio papel Then IllegalArgumentException")
    void givenAdmin_whenDemoteSelf_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO("Admin Editado", Role.FARMER);

        assertThatThrownBy(() -> adminUserUpdateUseCase.update(admin.getId(), dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O administrador não pode rebaixar o próprio papel.");

        verifyNoInteractions(userRepository, auditLogService);
    }

    @Test
    @DisplayName("Given não-ADMIN When update Then AccessDeniedException")
    void givenNonAdmin_whenUpdate_thenThrowsAccessDeniedException() {
        User manager = UserTestFactory.user().manager().build();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO("Qualquer Nome", Role.ADMIN);

        assertThatThrownBy(() -> adminUserUpdateUseCase.update(UUID.randomUUID(), dto, manager))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository, auditLogService);
    }

    @Test
    @DisplayName("Given usuário inexistente When update Then UserNotFoundException")
    void givenMissingUser_whenUpdate_thenThrowsUserNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID missingId = UUID.randomUUID();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO("Nome", Role.ADMIN);
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserUpdateUseCase.update(missingId, dto, admin))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }
}
