package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceUpdateDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateFieldOccurrenceUseCase {

    private final FieldOccurrenceRepository fieldOccurrenceRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public FieldOccurrence update(UUID id, FieldOccurrenceUpdateDTO dto, User loggedUser) {
        FieldOccurrence existing = fieldOccurrenceRepository.findById(id)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Ocorrência de campo não encontrada."));

        if (existing.getExecution() != null && existing.getExecution().getProductionPlan() != null) {
            accessValidator.validateWriteAccess(existing.getExecution().getProductionPlan().getFarmer(), loggedUser);
        }

        FieldOccurrence updated = FieldOccurrence.builder()
                .id(existing.getId())
                .execution(existing.getExecution())
                .logType(dto.logType() != null ? dto.logType() : existing.getLogType())
                .description(dto.description() != null && !dto.description().isBlank() ? dto.description().trim() : existing.getDescription())
                .weatherObservation(dto.weatherObservation() != null ? dto.weatherObservation().trim() : existing.getWeatherObservation())
                .evidenceUrl(dto.evidenceUrl() != null ? dto.evidenceUrl().trim() : existing.getEvidenceUrl())
                .createdAt(existing.getCreatedAt())
                .updatedAt(null)
                .deletedAt(existing.getDeletedAt())
                .build();

        return fieldOccurrenceRepository.save(updated);
    }
}
