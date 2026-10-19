package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class AuditLog {
    private final UUID id;
    private final UUID userId;
    private final AuditAction action;
    private final String targetTable;
    private final UUID targetId;
    private final Map<String, Object> details;
    private final LocalDateTime createdAt;
}
