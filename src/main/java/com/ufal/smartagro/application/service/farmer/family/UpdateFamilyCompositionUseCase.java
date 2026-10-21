package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionUpdateDTO;
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
public class UpdateFamilyCompositionUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public FamilyComposition update(UUID id, FamilyCompositionUpdateDTO dto, User loggedUser) {
        FamilyComposition existing = familyCompositionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro da composição familiar não encontrado."));

        accessValidator.validateWriteAccess(existing.getFarmer(), loggedUser);

        FamilyComposition updated = FamilyComposition.builder()
                .id(existing.getId())
                .farmer(existing.getFarmer())
                .name(dto.name() != null && !dto.name().isBlank() ? dto.name().trim() : existing.getName())
                .kinship(dto.kinship() != null && !dto.kinship().isBlank() ? dto.kinship().trim() : existing.getKinship())
                .age(dto.age() != null ? dto.age() : existing.getAge())
                .createdAt(existing.getCreatedAt())
                .updatedAt(null)
                .deletedAt(existing.getDeletedAt())
                .build();

        return familyCompositionRepository.save(updated);
    }
}
