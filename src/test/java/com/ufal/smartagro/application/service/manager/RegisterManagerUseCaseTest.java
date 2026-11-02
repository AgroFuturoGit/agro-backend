package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterManagerUseCase — cadastro de gestores por ADMIN e auditoria")
class RegisterManagerUseCaseTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserRegisterUseCase userRegisterUseCase;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RegisterManagerUseCase registerManagerUseCase;

    @Test
    @DisplayName("ADMIN cadastra gestor com sucesso e grava auditoria")
    void givenAdmin_whenRegisterManager_thenSuccessAndAudit() {
        User admin = UserTestFactory.user().admin().build();
        UUID orgId = UUID.randomUUID();
        Organization org = UserTestFactory.organization().id(orgId).build();
        User createdUser = UserTestFactory.user().manager().build();

        ManagerRegisterDTO dto = new ManagerRegisterDTO(
                "Gestor Teste", "gestor@smartagro.test", UserTestFactory.RAW_PASSWORD,
                UserTestFactory.VALID_CPF, LocalDate.of(1985, 5, 20));

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRegisterUseCase.createBaseUser(any(UserRegisterDTO.class))).thenReturn(createdUser);
        when(managerRepository.save(any(Manager.class))).thenAnswer(inv -> {
            Manager m = inv.getArgument(0);
            return new Manager(UUID.randomUUID(), m.getUser(), m.getOrganization(), null, null, null);
        });

        Manager saved = registerManagerUseCase.register(orgId, dto, admin);

        assertThat(saved).isNotNull();
        assertThat(saved.getUser()).isEqualTo(createdUser);
        assertThat(saved.getOrganization()).isEqualTo(org);

        verify(managerRepository).save(any(Manager.class));
        verify(auditLogService).record(eq(admin), eq(AuditAction.CREATE), eq("managers"), eq(saved.getId()), any());
    }

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

        verifyNoInteractions(organizationRepository, userRegisterUseCase, managerRepository, auditLogService);
    }

    @Test
    @DisplayName("Given null user When register Then AccessDeniedException")
    void givenNullUser_whenRegister_thenAccessDenied() {
        ManagerRegisterDTO dto = new ManagerRegisterDTO(
                "Gestor", "gestor@smartagro.test", UserTestFactory.RAW_PASSWORD,
                UserTestFactory.VALID_CPF, LocalDate.of(1980, 1, 1));

        assertThatThrownBy(() -> registerManagerUseCase.register(UUID.randomUUID(), dto, null))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(organizationRepository, userRegisterUseCase, managerRepository, auditLogService);
    }
}
