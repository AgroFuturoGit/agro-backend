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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllTechniciansUseCase {

    private final TechnicianRepository technicianRepository;
    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public List<Technician> findAll(User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Apenas administradores e gestores podem listar técnicos.");
        }

        if (loggedUser.getRole() == Role.ADMIN) {
            return technicianRepository.findAll().stream()
                    .filter(t -> t.getDeletedAt() == null)
                    .toList();
        } else if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));

            if (manager.getOrganization() == null) {
                return List.of();
            }

            Map<UUID, Technician> result = new LinkedHashMap<>();
            for (Technician t : technicianRepository.findAllByOrganizationId(manager.getOrganization().getId())) {
                if (t.getDeletedAt() == null && t.getId() != null) {
                    result.put(t.getId(), t);
                }
            }
            for (Technician t : technicianRepository.findAll()) {
                if (t.getDeletedAt() == null
                        && t.getId() != null
                        && t.getCreatedBy() != null
                        && loggedUser.getId() != null
                        && loggedUser.getId().equals(t.getCreatedBy().getId())) {
                    result.putIfAbsent(t.getId(), t);
                }
            }
            return new ArrayList<>(result.values());
        }

        throw new AccessDeniedException("Apenas administradores e gestores podem listar técnicos.");
    }
}
