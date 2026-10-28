package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.production.ProductionVersionGuard;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/** Atualiza a cultura após validar permissão e evitar duplicidade de nome/variedade. */
@Service
public class CropUpdateUseCase {

    private final CropRepository cropRepository;
    private final ProductionVersionGuard versionGuard;

    public CropUpdateUseCase(CropRepository cropRepository, ProductionVersionGuard versionGuard) {
        this.cropRepository = cropRepository;
        this.versionGuard = versionGuard != null ? versionGuard : new ProductionVersionGuard();
    }

    public CropUpdateUseCase(CropRepository cropRepository) {
        this(cropRepository, new ProductionVersionGuard());
    }

    /** Retrocompatibilidade sem baseUpdatedAt. */
    public Crop execute(UUID id, Crop updatedCropData, User loggedUser) {
        return execute(id, updatedCropData, loggedUser, null);
    }

    /** Preserva o identificador e grava também ciclo, produtividade, tipo e unidade atualizados, com controle de concorrência. */
    public Crop execute(UUID id, Crop updatedCropData, User loggedUser, LocalDateTime baseUpdatedAt) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar culturas.");
        }

        Crop existingCrop = cropRepository.findById(id)
                .orElseThrow(CropNotFoundException::new);

        versionGuard.ensureUpToDate(
                baseUpdatedAt,
                existingCrop.getUpdatedAt(),
                existingCrop.getCreatedAt(),
                Mapper.toCropResponseDTO(existingCrop),
                "A cultura foi alterada por outro usuário depois que esta edição começou."
        );

        String targetName = (updatedCropData.getName() != null && !updatedCropData.getName().isBlank())
                ? updatedCropData.getName()
                : existingCrop.getName();

        String targetVariety = (updatedCropData.getVariety() != null && !updatedCropData.getVariety().isBlank())
                ? updatedCropData.getVariety()
                : existingCrop.getVariety();

        boolean isNameOrVarietyChanged = !existingCrop.getName().equals(targetName) ||
                !existingCrop.getVariety().equals(targetVariety);

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

        return cropRepository.save(cropToSave);
    }
}
