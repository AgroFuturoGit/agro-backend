package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionVersionGuard;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Atualiza a cultura após validar permissão e evitar duplicidade de nome/variedade. */
@Service
@RequiredArgsConstructor
public class CropUpdateUseCase {

    private final CropRepository cropRepository;
    private final ProductionVersionGuard versionGuard;
    private final AuditLogService auditLogService;

    /** Retrocompatibilidade sem baseUpdatedAt. */
    @Transactional
    public Crop execute(UUID id, Crop updatedCropData, User loggedUser) {
        return execute(id, updatedCropData, loggedUser, null);
    }

    /** Preserva o identificador e grava também ciclo, produtividade, tipo e unidade atualizados, com controle de concorrência. */
    @Transactional
    public Crop execute(UUID id, Crop updatedCropData, User loggedUser, LocalDateTime baseUpdatedAt) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN)) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar culturas.");
        }

        Crop existingCrop = cropRepository.findById(id)
                .orElseThrow(CropNotFoundException::new);

        versionGuard.ensureUpToDate(
                baseUpdatedAt,
                existingCrop.getUpdatedAt(),
                existingCrop.getCreatedAt(),
                existingCrop,
                "A cultura foi alterada por outro usuário depois que esta edição começou."
        );

        String targetName = (updatedCropData.getName() != null && !updatedCropData.getName().isBlank())
                ? updatedCropData.getName().trim()
                : existingCrop.getName().trim();

        String targetVariety = (updatedCropData.getVariety() != null && !updatedCropData.getVariety().isBlank())
                ? updatedCropData.getVariety().trim()
                : existingCrop.getVariety().trim();

        boolean isNameOrVarietyChanged = !existingCrop.getName().trim().equals(targetName) ||
                !existingCrop.getVariety().trim().equals(targetVariety);

        if (isNameOrVarietyChanged && cropRepository.existsByNameAndVariety(targetName, targetVariety)) {
            throw new CropAlreadyExistsException();
        }

        Crop cropToSave = new Crop(
                existingCrop.getId(),
                targetName,
                targetVariety,
                updatedCropData.getIsPriority() != null ? updatedCropData.getIsPriority() : existingCrop.getIsPriority(),
                updatedCropData.getCycleDays() != null ? updatedCropData.getCycleDays() : existingCrop.getCycleDays(),
                updatedCropData.getExpectedProductivity() != null ? updatedCropData.getExpectedProductivity() : existingCrop.getExpectedProductivity(),
                updatedCropData.getHarvestType() != null ? updatedCropData.getHarvestType() : existingCrop.getHarvestType(),
                updatedCropData.getUnit() != null ? updatedCropData.getUnit() : existingCrop.getUnit(),
                updatedCropData.getUnitWeightKg() != null ? updatedCropData.getUnitWeightKg() : existingCrop.getUnitWeightKg(),
                existingCrop.getCreatedAt(),
                existingCrop.getUpdatedAt(),
                existingCrop.getDeletedAt()
        );

        Crop saved = cropRepository.save(cropToSave);

        Map<String, Object> details = new HashMap<>();
        details.put("name", saved.getName());
        details.put("variety", saved.getVariety());
        if (saved.getHarvestType() != null) {
            details.put("harvestType", saved.getHarvestType().name());
        }

        auditLogService.record(
                loggedUser,
                AuditAction.UPDATE,
                "crops",
                saved.getId(),
                details
        );

        return saved;
    }
}
