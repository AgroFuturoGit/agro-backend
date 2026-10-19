package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.AuditLog;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);
}
