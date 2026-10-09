package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.AdminUserUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserUpdateUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserUpdateUseCase adminUserUpdateUseCase;

    @Test
    @DisplayName("Given ADMIN When update Then muda nome, DOB e role e preserva email, CPF e senha")
    void givenAdmin_whenUpdate_thenKeepsEmailCpfAndPassword() {
        User admin = UserTestFactory.user().admin().build();
        User target = UserTestFactory.user()
                .technician()
                .email("tech@smartagro.test")
                .cpf(UserTestFactory.VALID_CPF)
                .password(UserTestFactory.ENCODED_PASSWORD)
                .build();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO(
                "Nome Admin Editado",
                Role.ADMIN
        );
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = adminUserUpdateUseCase.update(target.getId(), dto, admin);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("tech@smartagro.test");
        assertThat(saved.getCpf()).isEqualTo(UserTestFactory.VALID_CPF);
        assertThat(saved.getPassword()).isEqualTo(UserTestFactory.ENCODED_PASSWORD);
        assertThat(saved.getFullName()).isEqualTo("Nome Admin Editado");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.email()).isEqualTo(target.getEmail());
        assertThat(response.cpf()).isEqualTo(target.getCpf());
    }

    @ParameterizedTest(name = "Given Role.{0} When update Then AccessDeniedException")
    @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
    void givenNonAdmin_whenUpdate_thenAccessDenied(Role role) {
        User actor = UserTestFactory.user().role(role).build();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO("Nome", Role.ADMIN);

        assertThatThrownBy(() -> adminUserUpdateUseCase.update(UUID.randomUUID(), dto, actor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado. Somente administradores podem realizar esta ação.");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Given ADMIN When alvo inexistente Then UserNotFoundException")
    void givenAdmin_whenTargetMissing_thenUserNotFound() {
        User admin = UserTestFactory.user().admin().build();
        UUID missingId = UUID.randomUUID();
        AdminUserUpdateDTO dto = new AdminUserUpdateDTO("Nome", Role.ADMIN);
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserUpdateUseCase.update(missingId, dto, admin))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Usuário não encontrado!");

        verify(userRepository, never()).save(any());
    }
}
