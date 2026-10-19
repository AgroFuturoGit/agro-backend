package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.AuditLogEntity;
import com.ufal.smartagro.domain.model.AuditLog;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogMapperTest {

    private final AuditLogMapper mapper = new AuditLogMapper();

    @Test
    @DisplayName("Deve converter AuditLogEntity para AuditLog de domínio")
    void toDomain_converteCorretamente() {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setAction(AuditAction.VALIDATE);
        entity.setTargetTable("production_executions");
        entity.setTargetId(UUID.randomUUID());
        entity.setDetails(Map.of("quantityKg", 150.5));
        entity.setCreatedAt(LocalDateTime.now());

        AuditLog domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getUserId()).isEqualTo(entity.getUserId());
        assertThat(domain.getAction()).isEqualTo(entity.getAction());
        assertThat(domain.getTargetTable()).isEqualTo(entity.getTargetTable());
        assertThat(domain.getTargetId()).isEqualTo(entity.getTargetId());
        assertThat(domain.getDetails()).isEqualTo(entity.getDetails());
        assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }

    @Test
    @DisplayName("Deve converter AuditLog de domínio para AuditLogEntity")
    void toEntity_converteCorretamente() {
        AuditLog domain = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .action(AuditAction.REJECT)
                .targetTable("production_executions")
                .targetId(UUID.randomUUID())
                .details(Map.of("reason", "Qualidade"))
                .createdAt(LocalDateTime.now())
                .build();

        AuditLogEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getUserId()).isEqualTo(domain.getUserId());
        assertThat(entity.getAction()).isEqualTo(domain.getAction());
        assertThat(entity.getTargetTable()).isEqualTo(domain.getTargetTable());
        assertThat(entity.getTargetId()).isEqualTo(domain.getTargetId());
        assertThat(entity.getDetails()).isEqualTo(domain.getDetails());
        assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
    }

    @Test
    @DisplayName("Deve retornar nulo ao receber entradas nulas")
    void nullInputs_retornamNull() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }
}
