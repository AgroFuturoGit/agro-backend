package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFamilyCompositionUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public FamilyComposition update(UUID id, FamilyCompositionUpdateDTO dto, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN && loggedUser.getRole() != Role.FARMER)) {
            throw new AccessDeniedException("Apenas administradores, técnicos assistentes ou o próprio agricultor podem atualizar membros familiares.");
        }

        FamilyComposition existing = familyCompositionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membro da composição familiar não encontrado."));

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

        FamilyComposition saved = familyCompositionRepository.save(updated);

        Map<String, Object> details = new HashMap<>();
        details.put("farmerId", saved.getFarmer().getId().toString());
        details.put("name", saved.getName());
        details.put("kinship", saved.getKinship());

        auditLogService.record(
                loggedUser,
                AuditAction.UPDATE,
                "family_compositions",
                saved.getId(),
                details
        );

        return saved;
    }
}
