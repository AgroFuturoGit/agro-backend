package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
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

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll(User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException();
        }

        if (loggedUser.getRole() == Role.MANAGER) {
            Set<UUID> allowedUserIds = resolveAllowedUserIdsForManager(loggedUser);
            return userRepository.findAll()
                    .stream()
                    .filter(u -> allowedUserIds.contains(u.getId()))
                    .map(Mapper::toUserResponseDTO)
                    .collect(Collectors.toList());
        }

        return userRepository.findAll()
                .stream()
                .map(Mapper::toUserResponseDTO)
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

        for (Manager m : managerRepository.findAll()) {
            if (m.getOrganization() != null
                    && managerOrgId.equals(m.getOrganization().getId())
                    && m.getUser() != null
                    && m.getUser().getId() != null) {
                allowedUserIds.add(m.getUser().getId());
            }
        }

        for (Farmer f : farmerRepository.findAll()) {
            if (f.getCommunity() != null
                    && f.getCommunity().getOrganization() != null
                    && managerOrgId.equals(f.getCommunity().getOrganization().getId())
                    && f.getUser() != null
                    && f.getUser().getId() != null) {
                allowedUserIds.add(f.getUser().getId());
            }
        }

        return allowedUserIds;
    }
}
