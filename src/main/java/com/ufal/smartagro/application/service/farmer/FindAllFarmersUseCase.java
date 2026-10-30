package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindAllFarmersUseCase {

    private final FarmerRepository farmerRepository;
    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;
    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;

    @Transactional(readOnly = true)
    public List<Farmer> findAll(UUID communityId) {
        return findAll(communityId, null);
    }

    @Transactional(readOnly = true)
    public List<Farmer> findAll(UUID communityId, User loggedUser) {
        if (loggedUser != null && loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            if (managerOrgId == null) {
                throw new AccessDeniedException("Gestor sem organização vinculada.");
            }

            if (communityId != null) {
                Community community = communityRepository.findById(communityId)
                        .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));
                UUID communityOrgId = community.getOrganization() != null ? community.getOrganization().getId() : null;
                if (communityOrgId == null || !managerOrgId.equals(communityOrgId)) {
                    throw new AccessDeniedException("O gestor só tem acesso aos agricultores da sua própria organização.");
                }
                return farmerRepository.findAllByCommunityId(communityId);
            }

            return farmerRepository.findAllByOrganizationId(managerOrgId);
        }

        if (loggedUser != null && loggedUser.getRole() == Role.TECHNICIAN) {
            Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Técnico não encontrado."));

            if (communityId != null) {
                if (technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), communityId).isEmpty()) {
                    throw new AccessDeniedException("O técnico só pode visualizar agricultores das comunidades sob sua assistência.");
                }
                return farmerRepository.findAllByCommunityId(communityId);
            }

            Set<UUID> assistedCommunityIds = technicalAssistanceRepository.findByTechnicianId(technician.getId())
                    .stream()
                    .filter(TechnicalAssistance::isActive)
                    .map(ta -> ta.getCommunity() != null ? ta.getCommunity().getId() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return farmerRepository.findAllByCommunityIdIn(assistedCommunityIds);
        }

        if (communityId != null) {
            return farmerRepository.findAllByCommunityId(communityId);
        }
        return farmerRepository.findAll();
    }
}
