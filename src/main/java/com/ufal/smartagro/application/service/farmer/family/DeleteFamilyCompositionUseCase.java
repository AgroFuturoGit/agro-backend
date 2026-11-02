package com.ufal.smartagro.application.service.farmer.family;

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
public class DeleteFamilyCompositionUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN && loggedUser.getRole() != Role.FARMER)) {
            throw new AccessDeniedException("Apenas administradores, técnicos assistentes ou o próprio agricultor podem excluir membros familiares.");
        }

        FamilyComposition existing = familyCompositionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membro da composição familiar não encontrado."));

        accessValidator.validateWriteAccess(existing.getFarmer(), loggedUser);

        familyCompositionRepository.delete(id);

        Map<String, Object> details = new HashMap<>();
        details.put("farmerId", existing.getFarmer().getId().toString());
        details.put("name", existing.getName());

        auditLogService.record(
                loggedUser,
                AuditAction.DELETE,
                "family_compositions",
                id,
                details
        );
    }
}
