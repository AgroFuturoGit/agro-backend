package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicalAssistanceRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductionAccessValidator {

    static final String TECHNICIAN_WRITE_DENIED =
            "O técnico só pode alterar produção de agricultores sob sua assistência.";

    private final ManagerRepository managerRepository;
    private final TechnicianRepository technicianRepository;
    private final TechnicalAssistanceRepository technicalAssistanceRepository;

    public void validateAccess(Farmer farmer, User loggedUser) {
        if (farmer == null) return;

        if (loggedUser.getRole() == Role.FARMER) {
            if (farmer.getUser() == null || !farmer.getUser().getId().equals(loggedUser.getId())) {
                throw new AccessDeniedException("O agricultor só tem acesso aos seus próprios dados de produção.");
            }
        } else if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));

            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            UUID farmerOrgId = (farmer.getCommunity() != null && farmer.getCommunity().getOrganization() != null)
                    ? farmer.getCommunity().getOrganization().getId()
                    : null;

            if (managerOrgId == null || farmerOrgId == null || !managerOrgId.equals(farmerOrgId)) {
                throw new AccessDeniedException("O gestor só tem acesso aos agricultores da sua própria organização.");
            }
        }
    }

    public void validateWriteAccess(Farmer farmer, User loggedUser) {
        validateAccess(farmer, loggedUser);

        if (loggedUser.getRole() != Role.TECHNICIAN) {
            return;
        }

        Technician technician = technicianRepository.findByUserId(loggedUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Técnico não encontrado."));

        if (farmer == null || farmer.getId() == null || farmer.getCommunity() == null || farmer.getCommunity().getId() == null) {
            throw new AccessDeniedException(TECHNICIAN_WRITE_DENIED);
        }

        technicalAssistanceRepository.findActiveByTechnicianAndCommunity(technician.getId(), farmer.getCommunity().getId())
                .orElseThrow(() -> new AccessDeniedException(TECHNICIAN_WRITE_DENIED));
    }
}
