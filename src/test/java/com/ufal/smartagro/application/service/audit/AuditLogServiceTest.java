package com.ufal.smartagro.application.service.audit;

import com.ufal.smartagro.domain.model.AuditLog;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private User actor;

    @BeforeEach
    void setUp() {
        actor = User.builder()
                .id(UUID.randomUUID())
                .fullName("Admin Teste")
                .email("admin@smartagro.test")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Deve registrar log de auditoria com parâmetros válidos")
    void record_comParametrosValidos_salvaAuditLogCorretamente() {
        UUID targetId = UUID.randomUUID();
        Map<String, Object> details = Map.of("name", "Comunidade Centro", "organizationId", UUID.randomUUID().toString());

        auditLogService.record(actor, AuditAction.CREATE, "communities", targetId, details);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(actor.getId());
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(saved.getTargetTable()).isEqualTo("communities");
        assertThat(saved.getTargetId()).isEqualTo(targetId);
        assertThat(saved.getDetails()).containsEntry("name", "Comunidade Centro");
    }

    @Test
    @DisplayName("Deve sanitizar details removendo senhas, tokens e segredos")
    void record_comDadosSensiveis_sanitizaRemovendoSenhasETokens() {
        Map<String, Object> details = new HashMap<>();
        details.put("status", "ACTIVE");
        details.put("password", "123456");
        details.put("user_token", "xyz-jwt-token");
        details.put("client_secret", "secret-key");
        details.put("hashValue", "sha256-hash");

        auditLogService.record(actor, AuditAction.UPDATE, "users", UUID.randomUUID(), details);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getDetails())
                .containsEntry("status", "ACTIVE")
                .doesNotContainKeys("password", "user_token", "client_secret", "hashValue");
    }

    @Test
    @DisplayName("Deve aceitar details nulo salvando mapa vazio")
    void record_comDetailsNulo_salvaComMapaVazio() {
        auditLogService.record(actor, AuditAction.EXPORT, "reports", null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getTargetId()).isNull();
        assertThat(saved.getDetails()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ator for nulo ou sem ID")
    void record_semAtorOuSemId_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> auditLogService.record(null, AuditAction.CREATE, "communities", UUID.randomUUID(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ator responsável");

        User actorSemId = User.builder().fullName("Sem ID").build();
        assertThatThrownBy(() -> auditLogService.record(actorSemId, AuditAction.CREATE, "communities", UUID.randomUUID(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ator responsável");

        verify(auditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ação for nula")
    void record_semAcao_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> auditLogService.record(actor, null, "communities", UUID.randomUUID(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ação de auditoria");

        verify(auditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando tabela alvo for nula ou em branco")
    void record_semTabelaAlvo_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> auditLogService.record(actor, AuditAction.CREATE, null, UUID.randomUUID(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tabela alvo");

        assertThatThrownBy(() -> auditLogService.record(actor, AuditAction.CREATE, "   ", UUID.randomUUID(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tabela alvo");

        verify(auditLogRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
