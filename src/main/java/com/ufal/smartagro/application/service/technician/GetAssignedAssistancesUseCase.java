package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Lista assistências técnicas ativas com comunidade/organização e contagem de agricultores ativos. */
@Service
@RequiredArgsConstructor
public class GetAssignedAssistancesUseCase {

    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;
    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public List<AssignedAssistanceItem> getAssignedAssistances(UUID technicianId, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Acesso negado para visualizar assistências atribuídas.");
        }
        Role role = loggedUser.getRole();
        Technician technician = technicianRepository.findById(technicianId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));

        UUID managerOrgId = resolveAuthorizedManagerOrgId(role, technician, loggedUser);

        List<TechnicalAssistance> activeAssistances = assistanceRepository.findByTechnicianId(technicianId).stream()
                .filter(a -> a.getDeletedAt() == null && a.getEndDate() == null)
                .filter(a -> a.getCommunity() != null && a.getCommunity().getDeletedAt() == null)
                .filter(a -> managerOrgId == null || isFromOrganization(a.getCommunity(), managerOrgId))
                .toList();

        Map<UUID, Long> farmerCountsByCommunity = new HashMap<>();
        return activeAssistances.stream()
                .map(assistance -> {
                    UUID communityId = assistance.getCommunity().getId();
                    long activeFarmersCount = farmerCountsByCommunity.computeIfAbsent(
                            communityId, farmerRepository::countByCommunityId);
                    return new AssignedAssistanceItem(assistance, activeFarmersCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignedAssistanceItem> getAssignedAssistances(User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Acesso negado para visualizar assistências atribuídas.");
        }
        Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));
        return getAssignedAssistances(technician.getId(), loggedUser);
    }

    private UUID resolveAuthorizedManagerOrgId(Role role, Technician technician, User loggedUser) {
        if (role == Role.ADMIN) {
            return null;
        }
        if (role == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            if (manager.getOrganization() == null) {
                throw new AccessDeniedException("Gestor não possui organização vinculada.");
            }
            return manager.getOrganization().getId();
        }
        if (role == Role.TECHNICIAN) {
            if (technician.getUser() == null || !technician.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Técnico só pode visualizar suas próprias assistências.");
            }
            return null;
        }
        throw new AccessDeniedException("Acesso negado para visualizar assistências atribuídas.");
    }

    private boolean isFromOrganization(Community community, UUID organizationId) {
        return community.getOrganization() != null && organizationId.equals(community.getOrganization().getId());
    }

    public record AssignedAssistanceItem(TechnicalAssistance assistance, long activeFarmersCount) {}
}
