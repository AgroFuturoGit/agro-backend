package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FindAllUsersUseCase {

    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final FarmerRepository farmerRepository;
    private final TechnicianRepository technicianRepository;

    @Transactional(readOnly = true)
    public List<User> findAll(User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException();
        }

        if (loggedUser.getRole() == Role.MANAGER) {
            Set<UUID> allowedUserIds = resolveAllowedUserIdsForManager(loggedUser);
            if (allowedUserIds.isEmpty()) {
                return List.of();
            }
            return userRepository.findAllByIdIn(allowedUserIds);
        }

        return userRepository.findAll()
                .stream()
                .collect(Collectors.toList());
    }

    private Set<UUID> resolveAllowedUserIdsForManager(User loggedUser) {
        Manager manager = managerRepository.findByUserId(loggedUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
        UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
        if (managerOrgId == null) {
            throw new AccessDeniedException("Gestor sem organização vinculada.");
        }

        Set<UUID> allowedUserIds = new HashSet<>();
        allowedUserIds.add(loggedUser.getId());

        for (Manager m : managerRepository.findAllByOrganizationId(managerOrgId)) {
            if (m.getUser() != null && m.getUser().getId() != null) {
                allowedUserIds.add(m.getUser().getId());
            }
        }

        for (Farmer f : farmerRepository.findAllByOrganizationId(managerOrgId)) {
            if (f.getUser() != null && f.getUser().getId() != null) {
                allowedUserIds.add(f.getUser().getId());
            }
        }

        for (Technician t : technicianRepository.findAllByOrganizationId(managerOrgId)) {
            if (t.getUser() != null && t.getUser().getId() != null) {
                allowedUserIds.add(t.getUser().getId());
            }
        }

        if (loggedUser.getId() != null) {
            for (Technician t : technicianRepository.findAllByCreatedById(loggedUser.getId())) {
                if (t.getUser() != null && t.getUser().getId() != null) {
                    allowedUserIds.add(t.getUser().getId());
                }
            }
        }

        return allowedUserIds;
    }
}
