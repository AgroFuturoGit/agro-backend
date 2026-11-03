package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFarmerUseCase {

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;
    private final ProductionPlanRepository productionPlanRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID id) {
        delete(id, null);
    }

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser != null && loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Apenas administradores e gestores podem excluir agricultores.");
        }

        Farmer existingFarmer = farmerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agricultor não encontrado."));

        if (loggedUser != null) {
            accessValidator.validateAccess(existingFarmer, loggedUser);
        }

        int planCount = productionPlanRepository.findAllByFarmerId(id).size();
        if (planCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir um agricultor que possui " + planCount + " planos de produção vinculados."
            );
        }

        farmerRepository.delete(id);

        if (existingFarmer.getUser() != null && existingFarmer.getUser().getId() != null) {
            userRepository.softDelete(existingFarmer.getUser().getId());
        }

        if (loggedUser != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("fullName", existingFarmer.getFullName());
            if (existingFarmer.getUser() != null && existingFarmer.getUser().getId() != null) {
                details.put("linkedUserId", existingFarmer.getUser().getId().toString());
            }
            auditLogService.record(
                    loggedUser,
                    AuditAction.DELETE,
                    "farmers",
                    id,
                    details
            );
        }

        log.info("Produtor excluído logicamente [farmerId={}, linkedUserId={}] pelo usuário [actorId={}]",
                id,
                existingFarmer.getUser() != null ? existingFarmer.getUser().getId() : null,
                loggedUser != null ? loggedUser.getId() : "system");
    }
}
