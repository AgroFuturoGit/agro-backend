package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerRegisterDTO;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RegisterManagerUseCaseTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserRegisterUseCase userRegisterUseCase;

    @InjectMocks
    private RegisterManagerUseCase registerManagerUseCase;

    @ParameterizedTest(name = "Given Role.{0} When register Then AccessDeniedException")
    @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
    void givenNonAdmin_whenRegister_thenAccessDenied(Role role) {
        User actor = UserTestFactory.user().role(role).build();
        ManagerRegisterDTO dto = new ManagerRegisterDTO(
                "Gestor", "gestor@smartagro.test", UserTestFactory.RAW_PASSWORD,
                UserTestFactory.VALID_CPF, LocalDate.of(1980, 1, 1));

        assertThatThrownBy(() -> registerManagerUseCase.register(UUID.randomUUID(), dto, actor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Acesso negado. Somente administradores podem realizar esta ação.");

        verifyNoInteractions(organizationRepository, userRegisterUseCase, managerRepository);
    }

    @Test
    @DisplayName("Given MANAGER When tenta cadastrar outro gestor Then não promove ADMIN nem persiste")
    void givenManager_whenRegisterAnotherManager_thenAccessDenied() {
        User manager = UserTestFactory.user().manager().build();
        ManagerRegisterDTO dto = new ManagerRegisterDTO(
                "Outro Gestor", "outro@smartagro.test", UserTestFactory.RAW_PASSWORD,
                UserTestFactory.VALID_CPF, LocalDate.of(1982, 2, 2));
        Organization organization = UserTestFactory.organization().build();

        assertThatThrownBy(() -> registerManagerUseCase.register(organization.getId(), dto, manager))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(userRegisterUseCase, managerRepository);
    }
}
