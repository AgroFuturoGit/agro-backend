package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CropRegisterUseCase {

    private final CropRepository cropRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Crop execute(Crop crop, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN)) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar culturas.");
        }

        String targetName = crop.getName() != null ? crop.getName().trim() : null;
        String targetVariety = crop.getVariety() != null ? crop.getVariety().trim() : null;

        if (cropRepository.existsByNameAndVariety(targetName, targetVariety)) {
            throw new CropAlreadyExistsException();
        }

        Crop cropToSave = new Crop(
                crop.getId(),
                targetName,
                targetVariety,
                crop.getIsPriority(),
                crop.getCycleDays(),
                crop.getExpectedProductivity(),
                crop.getHarvestType(),
                crop.getUnit(),
                crop.getUnitWeightKg()
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
                AuditAction.CREATE,
                "crops",
                saved.getId(),
                details
        );

        return saved;
    }
}
