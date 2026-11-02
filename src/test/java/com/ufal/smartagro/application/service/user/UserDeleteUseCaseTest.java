package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
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
@DisplayName("UserDeleteUseCase — proteção contra autoexclusão e exclusão de usuário com perfil vinculado (API-03, API-12)")
class UserDeleteUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private TechnicianRepository technicianRepository;
    @Mock
    private ManagerRepository managerRepository;

    private UserDeleteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UserDeleteUseCase(userRepository, farmerRepository, technicianRepository, managerRepository);
    }

    @Test
    @DisplayName("ADMIN não pode excluir a própria conta (API-12)")
    void givenAdmin_whenDeleteSelf_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();

        assertThatThrownBy(() -> useCase.delete(admin.getId(), admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O administrador não pode excluir a própria conta.");

        verifyNoInteractions(userRepository, farmerRepository, technicianRepository, managerRepository);
    }

    @Test
    @DisplayName("ADMIN exclui outro usuário sem perfil ativo com sucesso")
    void givenAdmin_whenDeleteOtherUserWithoutProfile_thenSoftDeletes() {
        User admin = UserTestFactory.user().admin().build();
        User target = UserTestFactory.user().admin().build();
        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(farmerRepository.findByUserId(target.getId())).thenReturn(Optional.empty());
        when(technicianRepository.findByUserId(target.getId())).thenReturn(Optional.empty());
        when(managerRepository.findByUserId(target.getId())).thenReturn(Optional.empty());

        useCase.delete(target.getId(), admin);

        verify(userRepository).softDelete(target.getId());
    }

    @Test
    @DisplayName("ADMIN tenta excluir usuário com perfil ativo de agricultor -> EntityInUseException 409 (API-03)")
    void givenAdmin_whenDeleteUserWithActiveFarmerProfile_thenThrowsEntityInUseException() {
        User admin = UserTestFactory.user().admin().build();
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));
        when(farmerRepository.findByUserId(farmerUser.getId())).thenReturn(Optional.of(farmer));

        assertThatThrownBy(() -> useCase.delete(farmerUser.getId(), admin))
                .isInstanceOf(EntityInUseException.class)
                .hasMessage("Não é possível excluir diretamente um usuário vinculado a um perfil de agricultor. Utilize a rota de exclusão do perfil (/farmers).");

        verify(userRepository, never()).softDelete(any());
    }

    @Test
    @DisplayName("ADMIN tenta excluir usuário com perfil ativo de técnico -> EntityInUseException 409 (API-03)")
    void givenAdmin_whenDeleteUserWithActiveTechnicianProfile_thenThrowsEntityInUseException() {
        User admin = UserTestFactory.user().admin().build();
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        when(userRepository.findById(techUser.getId())).thenReturn(Optional.of(techUser));
        when(farmerRepository.findByUserId(techUser.getId())).thenReturn(Optional.empty());
        when(technicianRepository.findByUserId(techUser.getId())).thenReturn(Optional.of(technician));

        assertThatThrownBy(() -> useCase.delete(techUser.getId(), admin))
                .isInstanceOf(EntityInUseException.class)
                .hasMessage("Não é possível excluir diretamente um usuário vinculado a um perfil de técnico. Utilize a rota de exclusão do perfil (/technicians).");

        verify(userRepository, never()).softDelete(any());
    }

    @Test
    @DisplayName("ADMIN tenta excluir usuário com perfil ativo de gestor -> EntityInUseException 409 (API-03)")
    void givenAdmin_whenDeleteUserWithActiveManagerProfile_thenThrowsEntityInUseException() {
        User admin = UserTestFactory.user().admin().build();
        User managerUser = UserTestFactory.user().manager().build();
        Manager manager = UserTestFactory.managerProfile().user(managerUser).build();
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
        when(farmerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.empty());
        when(technicianRepository.findByUserId(managerUser.getId())).thenReturn(Optional.empty());
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> useCase.delete(managerUser.getId(), admin))
                .isInstanceOf(EntityInUseException.class)
                .hasMessage("Não é possível excluir diretamente um usuário vinculado a um perfil de gestor.");

        verify(userRepository, never()).softDelete(any());
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
