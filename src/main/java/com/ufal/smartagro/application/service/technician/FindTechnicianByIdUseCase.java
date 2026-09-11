package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindTechnicianByIdUseCase {

    private final TechnicianRepository technicianRepository;
    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public Technician findById(UUID id, User loggedUser) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado."));

        if (loggedUser.getRole() == Role.ADMIN) {
            return technician;
        } else if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));

            if (manager.getOrganization() != null) {
                UUID orgId = manager.getOrganization().getId();
                boolean exists = technicianRepository.existsByIdAndOrganizationId(id, orgId);
                if (exists) {
                    return technician;
                }
            }
            throw new AccessDeniedException("O gestor só tem acesso aos técnicos da sua própria organização.");
        }

        throw new AccessDeniedException("Apenas administradores e gestores podem consultar técnicos por ID.");
    }
}
