package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCommunityUseCase {

    private final CommunityRepository communityRepository;
    private final FarmerRepository farmerRepository;
    private final ManagerRepository managerRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID id) {
        delete(id, null);
    }

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser != null && loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Papel não autorizado para excluir comunidade.");
        }

        Community existingCommunity = communityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));

        if (loggedUser != null && loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            UUID communityOrgId = existingCommunity.getOrganization() != null ? existingCommunity.getOrganization().getId() : null;
            if (managerOrgId == null || communityOrgId == null || !managerOrgId.equals(communityOrgId)) {
                throw new AccessDeniedException("O gestor só pode excluir comunidades da sua própria organização.");
            }
        }

        int farmerCount = farmerRepository.findAllByCommunityId(id).size();
        if (farmerCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir uma comunidade que possui " + farmerCount + " agricultores vinculados."
            );
        }

        long activeAssistanceCount = technicalAssistanceRepository.findByCommunityId(id).stream()
                .filter(TechnicalAssistance::isActive)
                .count();
        if (activeAssistanceCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir uma comunidade que possui " + activeAssistanceCount + " assistências técnicas ativas vinculadas."
            );
        }

        communityRepository.delete(id);

        if (loggedUser != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("name", existingCommunity.getName());
            if (existingCommunity.getOrganization() != null && existingCommunity.getOrganization().getId() != null) {
                details.put("organizationId", existingCommunity.getOrganization().getId().toString());
            }
            auditLogService.record(
                    loggedUser,
                    AuditAction.DELETE,
                    "communities",
                    id,
                    details
            );
        }
    }
}
