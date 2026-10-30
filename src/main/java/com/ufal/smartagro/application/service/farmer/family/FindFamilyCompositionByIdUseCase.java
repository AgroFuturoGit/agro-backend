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
public class FindFamilyCompositionByIdUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public FamilyComposition findById(UUID id, User loggedUser) {
        FamilyComposition composition = familyCompositionRepository.findById(id)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Membro da composição familiar não encontrado."));

        accessValidator.validateAccess(composition.getFarmer(), loggedUser);

        return composition;
    }
}
