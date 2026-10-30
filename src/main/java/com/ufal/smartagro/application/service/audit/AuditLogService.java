package com.ufal.smartagro.application.service.audit;

import com.ufal.smartagro.domain.model.AuditLog;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.port.out.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(User actor, AuditAction action, String targetTable, UUID targetId, Map<String, Object> details) {
        if (actor == null || actor.getId() == null) {
            throw new IllegalArgumentException("Ator responsável pela ação é obrigatório para auditoria.");
        }
        if (action == null) {
            throw new IllegalArgumentException("Ação de auditoria é obrigatória.");
        }
        if (targetTable == null || targetTable.isBlank()) {
            throw new IllegalArgumentException("Tabela alvo é obrigatória para auditoria.");
        }

        Map<String, Object> sanitizedDetails = sanitizeDetails(details);

        AuditLog auditLog = AuditLog.builder()
                .userId(actor.getId())
                .action(action)
                .targetTable(targetTable.trim())
                .targetId(targetId)
                .details(sanitizedDetails)
                .build();

        auditLogRepository.save(auditLog);
    }

    private Map<String, Object> sanitizeDetails(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            String key = entry.getKey();
            if (isSensitiveKey(key)) {
                continue;
            }
            sanitized.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(sanitized);
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return true;
        }
        String lower = key.toLowerCase();
        return lower.contains("password") || lower.contains("senha") ||
               lower.contains("token") || lower.contains("secret") ||
               lower.contains("hash");
    }
}
