package com.ufal.smartagro.application.service.production.occurrence;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FieldOccurrenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFieldOccurrenceUseCase {

    private final FieldOccurrenceRepository fieldOccurrenceRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        FieldOccurrence existing = fieldOccurrenceRepository.findById(id)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Ocorrência de campo não encontrada."));

        if (existing.getExecution() != null && existing.getExecution().getProductionPlan() != null) {
            accessValidator.validateWriteAccess(existing.getExecution().getProductionPlan().getFarmer(), loggedUser);
        }

        fieldOccurrenceRepository.delete(id);
        log.info("Ocorrência de campo excluída logicamente [occurrenceId={}] pelo usuário [actorId={}, actorRole={}]",
                id, loggedUser.getId(), loggedUser.getRole());
    }
}
