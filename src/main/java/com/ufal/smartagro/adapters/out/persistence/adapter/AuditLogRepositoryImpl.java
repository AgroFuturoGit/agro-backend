package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.AuditLogEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.AuditLogMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaAuditLogRepository;
import com.ufal.smartagro.domain.model.AuditLog;
import com.ufal.smartagro.domain.port.out.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final JpaAuditLogRepository jpaAuditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = auditLogMapper.toEntity(auditLog);
        AuditLogEntity savedEntity = jpaAuditLogRepository.save(entity);
        return auditLogMapper.toDomain(savedEntity);
    }
}
