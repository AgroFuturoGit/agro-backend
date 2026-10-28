package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDeleteUseCase — proteção contra autoexclusão e exclusão lógica (API-12)")
class UserDeleteUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private UserDeleteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UserDeleteUseCase(userRepository);
    }

    @Test
    @DisplayName("ADMIN não pode excluir a própria conta (API-12)")
    void givenAdmin_whenDeleteSelf_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();

        assertThatThrownBy(() -> useCase.delete(admin.getId(), admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O administrador não pode excluir a própria conta.");

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("ADMIN exclui outro usuário existente com sucesso")
    void givenAdmin_whenDeleteOtherUser_thenSoftDeletes() {
        User admin = UserTestFactory.user().admin().build();
        User target = UserTestFactory.user().technician().build();
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        useCase.delete(target.getId(), admin);

        verify(userRepository).softDelete(target.getId());
    }

    @Test
    @DisplayName("Não-ADMIN não pode excluir usuário")
    void givenNonAdmin_whenDelete_thenThrowsAccessDeniedException() {
        User manager = UserTestFactory.user().manager().build();

        assertThatThrownBy(() -> useCase.delete(UUID.randomUUID(), manager))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Usuário inexistente lança UserNotFoundException")
    void givenAdmin_whenDeleteMissing_thenThrowsUserNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(missingId, admin))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).softDelete(any());
    }
}
