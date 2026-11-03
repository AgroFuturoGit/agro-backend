package com.ufal.smartagro.application.service.user;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindUserByIdUseCase {

    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final FarmerRepository farmerRepository;
    private final TechnicianRepository technicianRepository;

    @Transactional(readOnly = true)
    public User findById(UUID userId, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (loggedUser.getRole() == Role.ADMIN) {
            return user;
        }

        if (loggedUser.getId() != null && loggedUser.getId().equals(userId)) {
            return user;
        }

        if (loggedUser.getRole() == Role.MANAGER) {
            if (!isUserInManagerOrganization(userId, loggedUser)) {
                throw new AccessDeniedException("O gestor só tem acesso aos usuários da sua própria organização.");
            }
            return user;
        }

        if (loggedUser.getRole() == Role.FARMER || loggedUser.getRole() == Role.TECHNICIAN) {
            throw new AccessDeniedException("Você só tem acesso aos dados da sua própria conta de usuário.");
        }

        throw new AccessDeniedException("Não autorizado para consultar dados de usuários.");
    }

    private boolean isUserInManagerOrganization(UUID targetUserId, User loggedUser) {
        Manager manager = managerRepository.findByUserId(loggedUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
        UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
        if (managerOrgId == null) {
            return false;
        }
        if (targetUserId.equals(loggedUser.getId())) {
            return true;
        }

        boolean isOrgManager = managerRepository.findByUserId(targetUserId)
                .map(m -> m.getOrganization() != null && managerOrgId.equals(m.getOrganization().getId()))
                .orElse(false);
        if (isOrgManager) {
            return true;
        }

        boolean isOrgFarmer = farmerRepository.findByUserId(targetUserId)
                .map(f -> f.getCommunity() != null
                        && f.getCommunity().getOrganization() != null
                        && managerOrgId.equals(f.getCommunity().getOrganization().getId()))
                .orElse(false);
        if (isOrgFarmer) {
            return true;
        }

        return technicianRepository.findByUserId(targetUserId)
                .map(t -> technicianRepository.existsByIdAndOrganizationId(t.getId(), managerOrgId))
                .orElse(false);
    }
}
