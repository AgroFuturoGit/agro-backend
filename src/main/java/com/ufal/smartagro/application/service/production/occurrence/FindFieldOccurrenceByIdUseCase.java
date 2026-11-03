package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindFieldOccurrenceByIdUseCase {

    private final FieldOccurrenceRepository fieldOccurrenceRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public FieldOccurrence findById(UUID id, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        FieldOccurrence occurrence = fieldOccurrenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ocorrência de campo não encontrada."));

        if (occurrence.getExecution() != null && occurrence.getExecution().getProductionPlan() != null) {
            accessValidator.validateAccess(occurrence.getExecution().getProductionPlan().getFarmer(), loggedUser);
        }

        return occurrence;
    }
}
