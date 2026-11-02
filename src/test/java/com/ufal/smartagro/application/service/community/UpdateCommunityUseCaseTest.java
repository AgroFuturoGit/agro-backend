package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCommunityUseCase — autorização RBAC, IDOR e auditoria")
class UpdateCommunityUseCaseTest {

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private AuditLogService auditLogService;

    private UpdateCommunityUseCase useCase;

    private static final UUID COMM_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UpdateCommunityUseCase(communityRepository, managerRepository, auditLogService);
    }

    @Test
    @DisplayName("ADMIN atualiza comunidade com sucesso e grava auditoria")
    void givenAdmin_whenUpdate_thenSuccessAndAudit() {
        User admin = UserTestFactory.user().admin().build();
        Community existing = new Community(COMM_ID, "Comunidade Antiga", null, null, null, null);
        CommunityUpdateDTO dto = new CommunityUpdateDTO("Comunidade Nova");

        when(communityRepository.findById(COMM_ID)).thenReturn(Optional.of(existing));
        when(communityRepository.save(any(Community.class))).thenAnswer(i -> i.getArgument(0));

        Community result = useCase.update(COMM_ID, dto, admin);

        assertThat(result.getName()).isEqualTo("Comunidade Nova");
        verify(communityRepository).save(any(Community.class));
        verify(auditLogService).record(eq(admin), eq(AuditAction.UPDATE), eq("communities"), eq(COMM_ID), any());
    }

    @Test
    @DisplayName("MANAGER atualiza comunidade da própria organização com sucesso")
    void givenManagerOfSameOrg_whenUpdate_thenSuccess() {
        UUID orgId = UUID.randomUUID();
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        Organization org = new Organization(orgId, "Org Cooperativa", "11111111000111", OrganizationType.COOP, null, null, null);
        Community existing = new Community(COMM_ID, "Comunidade Antiga", org, null, null, null);
        Manager manager = new Manager(UUID.randomUUID(), managerUser, org, null, null, null);

        CommunityUpdateDTO dto = new CommunityUpdateDTO("Comunidade Renovada");

        when(communityRepository.findById(COMM_ID)).thenReturn(Optional.of(existing));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));
        when(communityRepository.save(any(Community.class))).thenAnswer(i -> i.getArgument(0));

        Community result = useCase.update(COMM_ID, dto, managerUser);

        assertThat(result.getName()).isEqualTo("Comunidade Renovada");
        verify(communityRepository).save(any(Community.class));
        verify(auditLogService).record(eq(managerUser), eq(AuditAction.UPDATE), eq("communities"), eq(COMM_ID), any());
    }

    @Test
    @DisplayName("MANAGER de outra organização tentando atualizar comunidade lança AccessDeniedException (IDOR)")
    void givenManagerOfDifferentOrg_whenUpdate_thenThrowsAccessDeniedException() {
        UUID orgAId = UUID.randomUUID();
        UUID orgBId = UUID.randomUUID();
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        Organization orgA = new Organization(orgAId, "Org A", "11111111000111", OrganizationType.COOP, null, null, null);
        Organization orgB = new Organization(orgBId, "Org B", "22222222000122", OrganizationType.ASSOC, null, null, null);

        Community existing = new Community(COMM_ID, "Comunidade da Org B", orgB, null, null, null);
        Manager manager = new Manager(UUID.randomUUID(), managerUser, orgA, null, null, null);

        CommunityUpdateDTO dto = new CommunityUpdateDTO("Comunidade Modificada");

        when(communityRepository.findById(COMM_ID)).thenReturn(Optional.of(existing));
        when(managerRepository.findByUserId(managerUser.getId())).thenReturn(Optional.of(manager));

        assertThatThrownBy(() -> useCase.update(COMM_ID, dto, managerUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("O gestor só pode atualizar comunidades da sua própria organização.");

        verify(communityRepository, never()).save(any());
        verify(auditLogService, never()).record(any(), any(), any(), any(), any());
    }

    @ParameterizedTest(name = "Given Role.{0} When update Then throws AccessDeniedException")
    @EnumSource(value = Role.class, names = {"TECHNICIAN", "FARMER"})
    void givenUnauthorizedRole_whenUpdate_thenThrowsAccessDeniedException(Role role) {
        User user = UserTestFactory.user().role(role).build();
        CommunityUpdateDTO dto = new CommunityUpdateDTO("Comunidade");

        assertThatThrownBy(() -> useCase.update(COMM_ID, dto, user))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Papel não autorizado para atualizar comunidade.");

        verify(communityRepository, never()).save(any());
        verify(auditLogService, never()).record(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Comunidade inexistente lança EntityNotFoundException")
    void givenNonExistentId_whenUpdate_thenThrowsEntityNotFoundException() {
        User admin = UserTestFactory.user().admin().build();
        when(communityRepository.findById(COMM_ID)).thenReturn(Optional.empty());

        CommunityUpdateDTO dto = new CommunityUpdateDTO("Novo Nome");

        assertThatThrownBy(() -> useCase.update(COMM_ID, dto, admin))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Comunidade não encontrada.");
    }
}
