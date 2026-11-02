package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
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
public class FindAllCommunitiesUseCase {

    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;
    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;
    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public List<Community> findAll(UUID orgId) {
        return findAll(orgId, null);
    }

    @Transactional(readOnly = true)
    public List<Community> findAll(UUID orgId, User loggedUser) {
        if (loggedUser == null) {
            if (orgId != null) {
                return communityRepository.findAllByOrganizationId(orgId);
            }
            return communityRepository.findAll();
        }

        Role role = loggedUser.getRole();

        if (role == Role.ADMIN) {
            if (orgId != null) {
                return communityRepository.findAllByOrganizationId(orgId);
            }
            return communityRepository.findAll();
        }

        if (role == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            if (managerOrgId == null) {
                throw new AccessDeniedException("Gestor sem organização vinculada.");
            }
            if (orgId != null && !orgId.equals(managerOrgId)) {
                throw new AccessDeniedException("O gestor só tem acesso às comunidades da sua própria organização.");
            }
            return communityRepository.findAllByOrganizationId(managerOrgId);
        }

        if (role == Role.TECHNICIAN) {
            Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Técnico não encontrado."));

            List<Community> assistedCommunities = technicalAssistanceRepository.findByTechnicianId(technician.getId())
                    .stream()
                    .filter(TechnicalAssistance::isActive)
                    .map(TechnicalAssistance::getCommunity)
                    .filter(c -> c != null && c.getDeletedAt() == null)
                    .distinct()
                    .collect(Collectors.toList());

            if (orgId != null) {
                return assistedCommunities.stream()
                        .filter(c -> c.getOrganization() != null && orgId.equals(c.getOrganization().getId()))
                        .collect(Collectors.toList());
            }
            return assistedCommunities;
        }

        if (role == Role.FARMER) {
            Farmer farmer = farmerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Agricultor não encontrado."));

            Community community = farmer.getCommunity();
            if (community == null || community.getDeletedAt() != null) {
                return List.of();
            }

            if (orgId != null) {
                UUID communityOrgId = community.getOrganization() != null ? community.getOrganization().getId() : null;
                if (!orgId.equals(communityOrgId)) {
                    throw new AccessDeniedException("O agricultor só tem acesso à sua própria comunidade.");
                }
            }

            return List.of(community);
        }

        throw new AccessDeniedException("Papel não autorizado para consultar comunidades.");
    }
}
