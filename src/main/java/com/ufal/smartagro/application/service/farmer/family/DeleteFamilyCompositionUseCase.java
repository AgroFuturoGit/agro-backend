package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteFamilyCompositionUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        FamilyComposition existing = familyCompositionRepository.findById(id)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Membro da composição familiar não encontrado."));

        accessValidator.validateWriteAccess(existing.getFarmer(), loggedUser);

        familyCompositionRepository.delete(id);
    }
}
