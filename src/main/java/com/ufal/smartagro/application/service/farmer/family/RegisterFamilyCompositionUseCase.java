package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterFamilyCompositionUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public FamilyComposition register(UUID farmerId, FamilyCompositionRegisterDTO dto, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN && loggedUser.getRole() != Role.FARMER)) {
            throw new AccessDeniedException("Apenas administradores, técnicos assistentes ou o próprio agricultor podem cadastrar membros familiares.");
        }

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new EntityNotFoundException("Agricultor não encontrado."));

        accessValidator.validateWriteAccess(farmer, loggedUser);

        FamilyComposition composition = FamilyComposition.builder()
                .farmer(farmer)
                .name(dto.name().trim())
                .kinship(dto.kinship().trim())
                .age(dto.age())
                .build();

        FamilyComposition saved = familyCompositionRepository.save(composition);

        Map<String, Object> details = new HashMap<>();
        details.put("farmerId", farmerId.toString());
        details.put("name", saved.getName());
        details.put("kinship", saved.getKinship());

        auditLogService.record(
                loggedUser,
                AuditAction.CREATE,
                "family_compositions",
                saved.getId(),
                details
        );

        return saved;
    }
}
