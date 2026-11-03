package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteOrganizationUseCase — validações de integridade relacional, RBAC e auditoria")
class DeleteOrganizationUseCaseTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private AuditLogService auditLogService;

    private DeleteOrganizationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteOrganizationUseCase(organizationRepository, communityRepository, managerRepository, auditLogService);
    }

    @Test
    @DisplayName("Quando não-ADMIN tenta excluir organização, lança AccessDeniedException")
    void givenNonAdmin_whenDelete_thenThrowsAccessDeniedException() {
        UUID orgId = UUID.randomUUID();
        User manager = UserTestFactory.user().manager().build();

        assertThatThrownBy(() -> useCase.delete(orgId, manager))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Apenas administradores podem excluir organizações.");

        verify(organizationRepository, never()).delete(any());
        verify(auditLogService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Quando existem comunidades vinculadas à organização, lança EntityInUseException (409)")
    void givenOrgWithCommunities_whenDelete_thenThrowsEntityInUseException() {
        UUID orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(mock(Organization.class)));
        when(communityRepository.findAllByOrganizationId(orgId)).thenReturn(List.of(mock(Community.class)));

        assertThatThrownBy(() -> useCase.delete(orgId))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("comunidades vinculadas");

        verify(organizationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando existem gestores vinculados à organização, lança EntityInUseException (409)")
    void givenOrgWithManagers_whenDelete_thenThrowsEntityInUseException() {
        UUID orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(mock(Organization.class)));
        when(communityRepository.findAllByOrganizationId(orgId)).thenReturn(List.of());
        when(managerRepository.countByOrganizationId(orgId)).thenReturn(2L);

        assertThatThrownBy(() -> useCase.delete(orgId))
                .isInstanceOf(EntityInUseException.class)
                .hasMessageContaining("gestores vinculados");

        verify(organizationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando a organização não existe, lança EntityNotFoundException (404)")
    void givenNonExistentOrg_whenDelete_thenThrowsEntityNotFoundException() {
        UUID orgId = UUID.randomUUID();
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(orgId))
                .isInstanceOf(EntityNotFoundException.class);

        verify(organizationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quando ADMIN exclui organização sem vínculos, deleta com sucesso e grava auditoria")
    void givenAdminAndOrgWithoutCommunitiesOrManagers_whenDelete_thenDeletesSuccessfullyWithAudit() {
        UUID orgId = UUID.randomUUID();
        User admin = UserTestFactory.user().admin().build();
        Organization org = UserTestFactory.organization().id(orgId).name("Cooperativa Alagoana").build();

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(communityRepository.findAllByOrganizationId(orgId)).thenReturn(List.of());
        when(managerRepository.countByOrganizationId(orgId)).thenReturn(0L);

        useCase.delete(orgId, admin);

        verify(organizationRepository).delete(orgId);
        verify(auditLogService).record(eq(admin), eq(AuditAction.DELETE), eq("organizations"), eq(orgId), any());
    }
}
