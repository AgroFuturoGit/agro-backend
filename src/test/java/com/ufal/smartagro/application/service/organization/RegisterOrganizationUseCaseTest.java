package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterOrganizationUseCase — cadastro de organizações, RBAC e auditoria")
class RegisterOrganizationUseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private AuditLogService auditLogService;

    private RegisterOrganizationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterOrganizationUseCase(organizationRepository, auditLogService);
    }

    @Test
    @DisplayName("ADMIN cadastra organização com sucesso e gera auditoria")
    void givenAdmin_whenRegister_thenSuccessAndAudit() {
        User admin = UserTestFactory.user().admin().build();
        OrganizationRegisterDTO dto = new OrganizationRegisterDTO("Coop Agro", "12345678000199", OrganizationType.COOP);

        when(organizationRepository.existsByTaxId(dto.taxId())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            return new Organization(UUID.randomUUID(), org.getName(), org.getTaxId(), org.getType(), null, null, null);
        });

        Organization saved = useCase.register(dto, admin);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Coop Agro");
        assertThat(saved.getTaxId()).isEqualTo("12345678000199");
        assertThat(saved.getType()).isEqualTo(OrganizationType.COOP);

        verify(organizationRepository).save(any(Organization.class));
        verify(auditLogService).record(
                eq(admin),
                eq(AuditAction.CREATE),
                eq("organizations"),
                eq(saved.getId()),
                argThat(map -> "Coop Agro".equals(map.get("name"))
                        && "12345678000199".equals(map.get("taxId"))
                        && "COOP".equals(map.get("type")))
        );
    }

    @ParameterizedTest(name = "Given Role.{0} When register Then lança AccessDeniedException")
    @EnumSource(value = Role.class, names = {"MANAGER", "TECHNICIAN", "FARMER"})
    void givenNonAdmin_whenRegister_thenThrowsAccessDeniedException(Role role) {
        User user = UserTestFactory.user().role(role).build();
        OrganizationRegisterDTO dto = new OrganizationRegisterDTO("Coop Agro", "12345678000199", OrganizationType.COOP);

        assertThatThrownBy(() -> useCase.register(dto, user))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores podem cadastrar organizações.");

        verify(organizationRepository, never()).save(any());
        verify(auditLogService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Quando usuário logado é nulo, lança AccessDeniedException")
    void givenNullUser_whenRegister_thenThrowsAccessDeniedException() {
        OrganizationRegisterDTO dto = new OrganizationRegisterDTO("Coop Agro", "12345678000199", OrganizationType.COOP);

        assertThatThrownBy(() -> useCase.register(dto, null))
                .isInstanceOf(AccessDeniedException.class);

        verify(organizationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Quando já existe organização com o mesmo taxId, lança IllegalArgumentException")
    void givenExistingTaxId_whenRegister_thenThrowsIllegalArgumentException() {
        User admin = UserTestFactory.user().admin().build();
        OrganizationRegisterDTO dto = new OrganizationRegisterDTO("Coop Agro", "12345678000199", OrganizationType.COOP);

        when(organizationRepository.existsByTaxId(dto.taxId())).thenReturn(true);

        assertThatThrownBy(() -> useCase.register(dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Já existe uma organização com este CNPJ/taxId");

        verify(organizationRepository, never()).save(any());
        verify(auditLogService, never()).record(any(), any(), any(), any(), any());
    }
}
