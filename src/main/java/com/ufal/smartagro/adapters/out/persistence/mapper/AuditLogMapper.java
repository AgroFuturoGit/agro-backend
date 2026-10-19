package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.AuditLogEntity;
import com.ufal.smartagro.domain.model.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return AuditLog.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .action(entity.getAction())
                .targetTable(entity.getTargetTable())
                .targetId(entity.getTargetId())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AuditLogEntity toEntity(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setAction(domain.getAction());
        entity.setTargetTable(domain.getTargetTable());
        entity.setTargetId(domain.getTargetId());
        entity.setDetails(domain.getDetails());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
