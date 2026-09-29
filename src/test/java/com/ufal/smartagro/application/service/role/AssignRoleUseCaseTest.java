package com.ufal.smartagro.application.service.role;

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
class AssignRoleUseCaseTest {

    private static final String PROFILE_ROUTE_MESSAGE =
            "Para criar ou promover gestores, produtores e técnicos, utilize as rotas específicas de perfil.";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignRoleUseCase assignRoleUseCase;

    @Nested
    @DisplayName("RBAC — somente ADMIN atribui papel")
    class Rbac {

        @ParameterizedTest(name = "Given Role.{0} When assignRole Then AccessDeniedException")
        @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
        void givenNonAdmin_whenAssignRole_thenAccessDenied(Role actorRole) {
            User actor = UserTestFactory.user().role(actorRole).build();

            assertThatThrownBy(() -> assignRoleUseCase.assignRole(UUID.randomUUID(), Role.ADMIN, actor))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Acesso negado. Somente administradores podem realizar esta ação.");

            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    @DisplayName("Papéis de perfil exigem rotas específicas")
    class ProfileRoles {

        @ParameterizedTest(name = "Given ADMIN When newRole={0} Then IllegalArgumentException")
        @EnumSource(value = Role.class, names = {"MANAGER", "FARMER", "TECHNICIAN"})
        void givenAdmin_whenAssignProfileRole_thenIllegalArgument(Role newRole) {
            User admin = UserTestFactory.user().admin().build();

            assertThatThrownBy(() -> assignRoleUseCase.assignRole(UUID.randomUUID(), newRole, admin))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(PROFILE_ROUTE_MESSAGE);

            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    @DisplayName("Happy path e not found")
    class HappyPath {

        @Test
        @DisplayName("Given ADMIN When atribui Role.ADMIN Then persiste o novo papel")
        void givenAdmin_whenAssignAdmin_thenPersistsRole() {
            User admin = UserTestFactory.user().admin().build();
            User target = UserTestFactory.user().technician().build();
            when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponseDTO response = assignRoleUseCase.assignRole(target.getId(), Role.ADMIN, admin);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
            assertThat(captor.getValue().getEmail()).isEqualTo(target.getEmail());
            assertThat(captor.getValue().getCpf()).isEqualTo(target.getCpf());
            assertThat(response.role()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Given ADMIN When usuário inexistente Then UserNotFoundException")
        void givenAdmin_whenUserMissing_thenUserNotFound() {
            User admin = UserTestFactory.user().admin().build();
            UUID missingId = UUID.randomUUID();
            when(userRepository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> assignRoleUseCase.assignRole(missingId, Role.ADMIN, admin))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("Usuário não encontrado!");

            verify(userRepository, never()).save(any());
        }
    }
}
