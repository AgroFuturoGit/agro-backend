package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserUpdateDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUpdateUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserUpdateUseCase userUpdateUseCase;

    @Nested
    @DisplayName("Happy path e imutabilidade de dados sensíveis")
    class Immutability {

        @Test
        @DisplayName("Given usuário existente When update Then altera só fullName e dateOfBirth")
        void givenExistingUser_whenUpdate_thenKeepsEmailCpfPasswordAndRole() {
            User logged = UserTestFactory.user()
                    .farmer()
                    .email("farmer@smartagro.test")
                    .cpf(UserTestFactory.VALID_CPF)
                    .password(UserTestFactory.ENCODED_PASSWORD)
                    .dateOfBirth(LocalDate.of(1985, 3, 12))
                    .build();
            UserUpdateDTO dto = UserTestFactory.updateDto()
                    .fullName("Nome Atualizado")
                    .dateOfBirth(LocalDate.of(1986, 4, 1))
                    .build();

            when(userRepository.findById(logged.getId())).thenReturn(Optional.of(logged));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponseDTO response = userUpdateUseCase.update(dto, logged);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getId()).isEqualTo(logged.getId());
            assertThat(saved.getFullName()).isEqualTo("Nome Atualizado");
            assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.of(1986, 4, 1));
            assertThat(saved.getEmail()).isEqualTo("farmer@smartagro.test");
            assertThat(saved.getCpf()).isEqualTo(UserTestFactory.VALID_CPF);
            assertThat(saved.getPassword()).isEqualTo(UserTestFactory.ENCODED_PASSWORD);
            assertThat(saved.getRole()).isEqualTo(Role.FARMER);
            assertThat(response.email()).isEqualTo(logged.getEmail());
            assertThat(response.cpf()).isEqualTo(logged.getCpf());
        }

        @ParameterizedTest(name = "Given Role.{0} When update Then carrega somente loggedUser.getId()")
        @EnumSource(Role.class)
        void givenAnyRole_whenUpdate_thenLoadsOnlyLoggedUserId(Role role) {
            User logged = UserTestFactory.user().role(role).build();
            UserUpdateDTO dto = UserTestFactory.updateDto().build();

            when(userRepository.findById(logged.getId())).thenReturn(Optional.of(logged));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            userUpdateUseCase.update(dto, logged);

            verify(userRepository).findById(logged.getId());
        }
    }

    @Nested
    @DisplayName("Exceções de domínio")
    class DomainExceptions {

        @Test
        @DisplayName("Given usuário inexistente When update Then UserNotFoundException")
        void givenMissingUser_whenUpdate_thenUserNotFound() {
            User logged = UserTestFactory.user().admin().build();
            UserUpdateDTO dto = UserTestFactory.updateDto().build();
            when(userRepository.findById(logged.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userUpdateUseCase.update(dto, logged))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage("Usuário não encontrado!");

            verify(userRepository, never()).save(any());
        }
    }
}
