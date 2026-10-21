package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FieldOccurrenceEntity;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FieldOccurrenceMapper {

    private final ProductionExecutionMapper executionMapper;

    public FieldOccurrence toDomain(FieldOccurrenceEntity entity) {
        if (entity == null) return null;
        return FieldOccurrence.builder()
                .id(entity.getId())
                .execution(executionMapper.toDomain(entity.getExecution()))
                .logType(entity.getLogType())
                .description(entity.getDescription())
                .weatherObservation(entity.getWeatherObservation())
                .evidenceUrl(entity.getEvidenceUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    public FieldOccurrenceEntity toEntity(FieldOccurrence domain) {
        if (domain == null) return null;
        FieldOccurrenceEntity entity = new FieldOccurrenceEntity();
        entity.setId(domain.getId());
        entity.setExecution(executionMapper.toEntity(domain.getExecution()));
        entity.setLogType(domain.getLogType());
        entity.setDescription(domain.getDescription());
        entity.setWeatherObservation(domain.getWeatherObservation());
        entity.setEvidenceUrl(domain.getEvidenceUrl());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }
}
