package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAssignedFarmersUseCase {

    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository assistanceRepository;
    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public List<Farmer> getAssignedFarmers(UUID technicianId, User loggedUser) {
        Role role = loggedUser.getRole();
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Técnico não encontrado."));

        if (role == Role.ADMIN) {
            // allowed
        } else if (role == Role.MANAGER) {
            var manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            // Verify that the technician's farmers belong to the manager's organization
            // We will filter later; if none belong, deny access
            // No explicit check needed here, will filter results
        } else if (role == Role.TECHNICIAN) {
            // Technician can only view own assigned farmers
            if (!technician.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("Técnico só pode visualizar seus próprios agricultores.");
            }
        } else {
            throw new AccessDeniedException("Acesso negado para visualizar agricultores atribuídos.");
        }

        List<TechnicalAssistance> assistances = assistanceRepository.findByTechnicianId(technicianId);
        // Consider only active assistances (endDate == null)
        List<Farmer> farmers = assistances.stream()
                .filter(a -> a.getEndDate() == null)
                .map(TechnicalAssistance::getFarmer)
                .collect(Collectors.toList());

        // If manager, filter farmers to those in manager's organization
        if (role == Role.MANAGER) {
            var manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization().getId();
            farmers = farmers.stream()
                    .filter(p -> p.getCommunity() != null && p.getCommunity().getOrganization() != null &&
                            managerOrgId.equals(p.getCommunity().getOrganization().getId()))
                    .collect(Collectors.toList());
        }
        return farmers;
    }
}
