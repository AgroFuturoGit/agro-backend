package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAssignedCommunitiesUseCase {

    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public List<Community> getAssignedCommunities(UUID technicianId, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Acesso negado para visualizar comunidades atribuídas.");
        }
        Role role = loggedUser.getRole();
        Technician technician = technicianRepository.findById(technicianId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));

        Manager manager = null;
        if (role == Role.ADMIN) {
            // allowed
        } else if (role == Role.MANAGER) {
            manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            if (manager.getOrganization() == null) {
                throw new AccessDeniedException("Gestor não possui organização vinculada.");
            }
        } else if (role == Role.TECHNICIAN) {
            if (!technician.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Técnico só pode visualizar suas próprias comunidades.");
            }
        } else {
            throw new AccessDeniedException("Acesso negado para visualizar comunidades atribuídas.");
        }

        List<TechnicalAssistance> assistances = assistanceRepository.findByTechnicianId(technicianId);
        List<Community> communities = assistances.stream()
                .filter(a -> a.getDeletedAt() == null && a.getEndDate() == null)
                .map(TechnicalAssistance::getCommunity)
                .filter(c -> c != null && c.getDeletedAt() == null)
                .collect(Collectors.toList());

        if (role == Role.MANAGER) {
            UUID managerOrgId = manager.getOrganization().getId();
            communities = communities.stream()
                    .filter(c -> c.getOrganization() != null &&
                            managerOrgId.equals(c.getOrganization().getId()))
                    .collect(Collectors.toList());
        }
        return communities;
    }

    @Transactional(readOnly = true)
    public List<Community> getAssignedCommunities(User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Acesso negado para visualizar comunidades atribuídas.");
        }
        Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));
        return getAssignedCommunities(technician.getId(), loggedUser);
    }
}
