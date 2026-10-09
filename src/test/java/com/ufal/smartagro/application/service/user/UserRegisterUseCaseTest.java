package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CpfAlreadyExistsException;
import com.ufal.smartagro.domain.exception.EmailAlreadyExistsException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Organization;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegisterUseCaseTest {

    private static final String ACCESS_DENIED_MESSAGE =
            "Acesso negado. Somente administradores podem realizar esta ação.";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegisterUseCase userRegisterUseCase;

    @Nested
    @DisplayName("Happy path — ADMIN registra papéis permitidos neste use case")
    class HappyPath {

        @ParameterizedTest(name = "Given ADMIN When registra Role.{0} Then persiste usuário com senha encoded")
        @EnumSource(value = Role.class, names = {"ADMIN", "TECHNICIAN"})
        void givenAdmin_whenRegisterAllowedRole_thenReturnsPersistedUser(Role targetRole) {
            User admin = UserTestFactory.user().admin().build();
            UserRegisterDTO dto = UserTestFactory.registerDto().role(targetRole).build();
            UUID savedId = UUID.randomUUID();

            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(userRepository.existsByCpf(dto.cpf())).thenReturn(false);
            when(passwordEncoder.encode(dto.password())).thenReturn(UserTestFactory.ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User incoming = invocation.getArgument(0);
                return new User(
                        savedId,
                        incoming.getFullName(),
                        incoming.getEmail(),
                        incoming.getPassword(),
                        incoming.getCpf(),
                        incoming.getRole()
                );
            });

            UserResponseDTO response = userRegisterUseCase.register(dto, admin);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User persisted = captor.getValue();

            assertThat(persisted.getId()).isNull();
            assertThat(persisted.getPassword()).isEqualTo(UserTestFactory.ENCODED_PASSWORD);
            assertThat(persisted.getRole()).isEqualTo(targetRole);
            assertThat(response.id()).isEqualTo(savedId);
            assertThat(response.email()).isEqualTo(dto.email());
            assertThat(response.cpf()).isEqualTo(dto.cpf());
            assertThat(response.role()).isEqualTo(targetRole);
            verify(passwordEncoder).encode(UserTestFactory.RAW_PASSWORD);
        }
    }

    @Nested
    @DisplayName("RBAC — somente ADMIN pode chamar register()")
    class RbacBlocking {

        @Test
        @DisplayName("Given MANAGER When tenta cadastrar ADMIN Then AccessDeniedException e nenhum save")
        void givenManager_whenRegisterAdmin_thenAccessDenied() {
            User manager = UserTestFactory.user().manager().build();
            UserRegisterDTO dto = UserTestFactory.registerDto().role(Role.ADMIN).build();

            assertThatThrownBy(() -> userRegisterUseCase.register(dto, manager))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ACCESS_DENIED_MESSAGE);

            verifyNoInteractions(userRepository, passwordEncoder);
        }

        @ParameterizedTest(name = "Given Role.{0} When register Then AccessDeniedException")
        @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
        void givenNonAdmin_whenRegister_thenAccessDenied(Role actorRole) {
            User actor = UserTestFactory.user().role(actorRole).build();
            UserRegisterDTO dto = UserTestFactory.registerDto().role(Role.TECHNICIAN).build();

            assertThatThrownBy(() -> userRegisterUseCase.register(dto, actor))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ACCESS_DENIED_MESSAGE);

            verifyNoInteractions(userRepository, passwordEncoder);
        }
    }

    @Nested
    @DisplayName("Isolamento de tenant — UserRegisterUseCase não vincula Organization/Community")
    class TenantIsolation {

        @Test
        @DisplayName("Given MANAGER da org A When tenta registrar usuário Then acesso negado (não há promoção nem vínculo cruzado)")
        void givenManagerOfOrganizationA_whenRegisterUser_thenCannotCreateAcrossTenants() {
            Organization orgA = UserTestFactory.organization().name("Cooperativa A").build();
            Organization orgB = UserTestFactory.organization().name("Cooperativa B").build();
            Community communityB = UserTestFactory.community().organization(orgB).build();
            User managerOfA = UserTestFactory.user().manager().build();
            UserRegisterDTO dto = UserTestFactory.registerDto().role(Role.FARMER).build();

            assertThat(orgA.getId()).isNotEqualTo(communityB.getOrganization().getId());
            assertThatThrownBy(() -> userRegisterUseCase.register(dto, managerOfA))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage(ACCESS_DENIED_MESSAGE);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Unicidade e persistência em createBaseUser")
    class CreateBaseUser {

        @Test
        @DisplayName("Given email já existente When createBaseUser Then EmailAlreadyExistsException")
        void givenExistingEmail_whenCreateBaseUser_thenEmailAlreadyExists() {
            UserRegisterDTO dto = UserTestFactory.registerDto().build();
            when(userRepository.existsByEmail(dto.email())).thenReturn(true);

            assertThatThrownBy(() -> userRegisterUseCase.createBaseUser(dto))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("O email informado já está em uso.");

            verify(userRepository, never()).existsByCpf(anyString());
            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Given CPF já existente When createBaseUser Then CpfAlreadyExistsException")
        void givenExistingCpf_whenCreateBaseUser_thenCpfAlreadyExists() {
            UserRegisterDTO dto = UserTestFactory.registerDto().build();
            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(userRepository.existsByCpf(dto.cpf())).thenReturn(true);

            assertThatThrownBy(() -> userRegisterUseCase.createBaseUser(dto))
                    .isInstanceOf(CpfAlreadyExistsException.class)
                    .hasMessage("O CPF informado já está em uso.");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Given email e CPF duplicados When createBaseUser Then falha primeiro por email")
        void givenEmailAndCpfDuplicated_whenCreateBaseUser_thenFailsOnEmailFirst() {
            UserRegisterDTO dto = UserTestFactory.registerDto().build();
            when(userRepository.existsByEmail(dto.email())).thenReturn(true);

            assertThatThrownBy(() -> userRegisterUseCase.createBaseUser(dto))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).existsByCpf(anyString());
        }
    }
}
