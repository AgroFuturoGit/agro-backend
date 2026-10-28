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

        String description = existing.getDescription();
        if (dto.description() != null) {
            String trimmed = dto.description().trim();
            if (!trimmed.isEmpty()) {
                description = trimmed;
            }
        }

        String weatherObservation = existing.getWeatherObservation();
        if (dto.weatherObservation() != null) {
            String trimmed = dto.weatherObservation().trim();
            weatherObservation = trimmed.isEmpty() ? null : trimmed;
        }

        String evidenceUrl = existing.getEvidenceUrl();
        if (dto.evidenceUrl() != null) {
            String trimmed = dto.evidenceUrl().trim();
            evidenceUrl = trimmed.isEmpty() ? null : trimmed;
        }

        FieldOccurrence updated = FieldOccurrence.builder()
                .id(existing.getId())
                .execution(existing.getExecution())
                .logType(dto.logType() != null ? dto.logType() : existing.getLogType())
                .description(description)
                .weatherObservation(weatherObservation)
                .evidenceUrl(evidenceUrl)
                .createdAt(existing.getCreatedAt())
                .updatedAt(null)
                .deletedAt(existing.getDeletedAt())
                .build();

        return fieldOccurrenceRepository.save(updated);
    }
}
