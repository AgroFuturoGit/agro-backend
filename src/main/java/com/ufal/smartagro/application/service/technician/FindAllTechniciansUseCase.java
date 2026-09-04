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

@Service
@RequiredArgsConstructor
public class FindAllTechniciansUseCase {

    private final TechnicianRepository technicianRepository;
    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public List<Technician> findAll(User loggedUser) {
        if (loggedUser.getRole() == Role.ADMIN) {
            return technicianRepository.findAll();
        } else if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));

            if (manager.getOrganization() == null) {
                return List.of();
            }
            return technicianRepository.findAllByOrganizationId(manager.getOrganization().getId());
        }

        throw new AccessDeniedException("Apenas administradores e gestores podem listar técnicos.");
    }
}
