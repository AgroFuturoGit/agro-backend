package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterCommunityUseCase — cadastro de comunidade e auditoria")
class RegisterCommunityUseCaseTest {

    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RegisterCommunityUseCase registerCommunityUseCase;

    @Test
    @DisplayName("ADMIN cadastra comunidade com sucesso e gera log de auditoria")
    void register_comAdmin_cadastraComunidadeEGeraAuditoria() {
        User admin = UserTestFactory.user().admin().build();
        UUID orgId = UUID.randomUUID();
        Organization org = new Organization(orgId, "Cooperativa Central", "12345678000199", OrganizationType.COOP, null, null, null);
        CommunityRegisterDTO dto = new CommunityRegisterDTO("Comunidade Boa Esperança");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(communityRepository.save(any(Community.class))).thenAnswer(inv -> {
            Community c = inv.getArgument(0);
            return new Community(UUID.randomUUID(), c.getName(), c.getOrganization(), null, null, null);
        });

        Community result = registerCommunityUseCase.register(orgId, dto, admin);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Comunidade Boa Esperança");
        assertThat(result.getOrganization()).isEqualTo(org);

        verify(auditLogService).record(
                eq(admin),
                eq(AuditAction.CREATE),
                eq("communities"),
                eq(result.getId()),
                argThat(map -> "Comunidade Boa Esperança".equals(map.get("name"))
                        && orgId.toString().equals(map.get("organizationId")))
        );
    }

    @Test
    @DisplayName("MANAGER cadastra comunidade com sucesso e gera log de auditoria")
    void register_comManager_cadastraComunidadeEGeraAuditoria() {
        User manager = UserTestFactory.user().role(Role.MANAGER).build();
        UUID orgId = UUID.randomUUID();
        Organization org = new Organization(orgId, "Associação Regional", "98765432000188", OrganizationType.ASSOC, null, null, null);
        CommunityRegisterDTO dto = new CommunityRegisterDTO("Comunidade Vale Verde");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(communityRepository.save(any(Community.class))).thenAnswer(inv -> {
            Community c = inv.getArgument(0);
            return new Community(UUID.randomUUID(), c.getName(), c.getOrganization(), null, null, null);
        });

        Community result = registerCommunityUseCase.register(orgId, dto, manager);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Comunidade Vale Verde");

        verify(auditLogService).record(
                eq(manager),
                eq(AuditAction.CREATE),
                eq("communities"),
                eq(result.getId()),
                any()
        );
    }

    @Test
    @DisplayName("FARMER não tem permissão para cadastrar comunidade e auditoria não é executada")
    void register_comFarmer_lancaAccessDeniedENaoAudita() {
        User farmer = UserTestFactory.user().farmer().build();
        UUID orgId = UUID.randomUUID();
        CommunityRegisterDTO dto = new CommunityRegisterDTO("Comunidade Inválida");

        assertThatThrownBy(() -> registerCommunityUseCase.register(orgId, dto, farmer))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(organizationRepository, communityRepository, auditLogService);
    }

    @Test
    @DisplayName("Organização inexistente lança IllegalArgumentException e auditoria não é executada")
    void register_organizacaoInexistente_lancaExcecaoENaoAudita() {
        User admin = UserTestFactory.user().admin().build();
        UUID orgId = UUID.randomUUID();
        CommunityRegisterDTO dto = new CommunityRegisterDTO("Comunidade Sem Org");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerCommunityUseCase.register(orgId, dto, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organização não encontrada");

        verify(communityRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }
}
