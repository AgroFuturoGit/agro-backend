package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.AuditLogEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataAccessException;
import com.ufal.smartagro.config.security.persistence.JpaAuditingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class AuditLogMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JpaAuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Deve inserir registro de auditoria com sucesso")
    void insert_salvaComSucesso() {
        UserEntity user = persistUser("actor@smartagro.test", "11122233344");

        AuditLogEntity log = new AuditLogEntity();
        log.setUserId(user.getId());
        log.setAction(AuditAction.CREATE);
        log.setTargetTable("communities");
        log.setTargetId(UUID.randomUUID());
        log.setDetails(Map.of("name", "Comunidade Centro"));

        AuditLogEntity saved = auditLogRepository.saveAndFlush(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
    }

    @Test
    @DisplayName("Trigger deve bloquear UPDATE na tabela audit_logs")
    void trigger_bloqueiaUpdate() {
        UserEntity user = persistUser("actor.update@smartagro.test", "22233344455");

        AuditLogEntity log = new AuditLogEntity();
        log.setUserId(user.getId());
        log.setAction(AuditAction.CREATE);
        log.setTargetTable("communities");
        log.setTargetId(UUID.randomUUID());
        AuditLogEntity saved = auditLogRepository.saveAndFlush(log);

        assertThatThrownBy(() -> jdbcTemplate.execute(
                String.format("UPDATE audit_logs SET action = 'UPDATE' WHERE id = '%s'", saved.getId())))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("audit_logs é append-only: operação UPDATE não permitida");
    }

    @Test
    @DisplayName("Trigger deve bloquear DELETE na tabela audit_logs")
    void trigger_bloqueiaDelete() {
        UserEntity user = persistUser("actor.delete@smartagro.test", "33344455566");

        AuditLogEntity log = new AuditLogEntity();
        log.setUserId(user.getId());
        log.setAction(AuditAction.VALIDATE);
        log.setTargetTable("production_executions");
        log.setTargetId(UUID.randomUUID());
        AuditLogEntity saved = auditLogRepository.saveAndFlush(log);

        assertThatThrownBy(() -> jdbcTemplate.execute(
                String.format("DELETE FROM audit_logs WHERE id = '%s'", saved.getId())))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("audit_logs é append-only: operação DELETE não permitida");
    }

    @Test
    @DisplayName("Trigger deve bloquear TRUNCATE na tabela audit_logs")
    void trigger_bloqueiaTruncate() {
        assertThatThrownBy(() -> jdbcTemplate.execute("TRUNCATE TABLE audit_logs"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("audit_logs é append-only");
    }

    @Test
    @DisplayName("Schema físico não deve conter colunas updated_at nem deleted_at")
    void schema_semColunasMutaveis() {
        List<String> columns = jdbcTemplate.query(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'audit_logs'",
                (rs, rowNum) -> rs.getString("column_name")
        );

        assertThat(columns)
                .contains("id", "user_id", "action", "target_table", "target_id", "details", "created_at")
                .doesNotContain("updated_at", "deleted_at");
    }

    private UserEntity persistUser(String email, String cpf) {
        UserEntity user = new UserEntity();
        user.setFullName("Ator de Auditoria");
        user.setEmail(email);
        user.setPassword("password123");
        user.setCpf(cpf);
        user.setRole(Role.ADMIN);
        user.setCreatedAt(LocalDateTime.now());
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }
}
