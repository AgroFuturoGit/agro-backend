package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindCommunityByIdUseCase {

    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;
    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;
    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public Community findById(UUID id) {
        return findById(id, null);
    }

    @Transactional(readOnly = true)
    public Community findById(UUID id, User loggedUser) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));

        if (loggedUser == null) {
            return community;
        }

        Role role = loggedUser.getRole();

        if (role == Role.ADMIN) {
            return community;
        }

        if (role == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            UUID communityOrgId = community.getOrganization() != null ? community.getOrganization().getId() : null;
            if (managerOrgId == null || communityOrgId == null || !managerOrgId.equals(communityOrgId)) {
                throw new AccessDeniedException("O gestor só tem acesso às comunidades da sua própria organização.");
            }
            return community;
        }

        if (role == Role.TECHNICIAN) {
            Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Técnico não encontrado."));
            boolean hasAssistance = technicalAssistanceRepository
                    .findActiveByTechnicianAndCommunity(technician.getId(), id)
                    .isPresent();
            if (!hasAssistance) {
                throw new AccessDeniedException("O técnico só tem acesso às comunidades sob sua assistência técnica ativa.");
            }
            return community;
        }

        if (role == Role.FARMER) {
            Farmer farmer = farmerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Agricultor não encontrado."));
            UUID farmerCommunityId = farmer.getCommunity() != null ? farmer.getCommunity().getId() : null;
            if (farmerCommunityId == null || !farmerCommunityId.equals(id)) {
                throw new AccessDeniedException("O agricultor só tem acesso à sua própria comunidade.");
            }
            return community;
        }

        throw new AccessDeniedException("Papel não autorizado para consultar comunidade.");
    }
}
