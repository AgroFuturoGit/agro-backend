package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CropDeleteUseCase {

    private final CropRepository cropRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void execute(UUID id, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN)) {
            throw new AccessDeniedException("Usuário não tem permissão para deletar uma cultura.");
        }

        Crop existingCrop = cropRepository.findById(id)
                .orElseThrow(CropNotFoundException::new);

        long planCount = productionPlanRepository.countByCropId(id);
        if (planCount > 0 || productionPlanRepository.existsByCropId(id)) {
            long effectiveCount = planCount > 0 ? planCount : 1L;
            throw new EntityInUseException(
                    "Não é possível excluir uma cultura que possui " + effectiveCount + " planos de produção vinculados."
            );
        }

        cropRepository.deleteById(id);

        Map<String, Object> details = new HashMap<>();
        details.put("name", existingCrop.getName());
        details.put("variety", existingCrop.getVariety());

        auditLogService.record(
                loggedUser,
                AuditAction.DELETE,
                "crops",
                id,
                details
        );

        log.info("Cultura excluída logicamente [cropId={}] pelo usuário [actorId={}, actorRole={}]",
                id, loggedUser.getId(), loggedUser.getRole());
    }
}
