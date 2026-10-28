package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllCommunitiesUseCase {

    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public List<Community> findAll(UUID orgId) {
        return findAll(orgId, null);
    }

    @Transactional(readOnly = true)
    public List<Community> findAll(UUID orgId, User loggedUser) {
        if (loggedUser != null && loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            if (managerOrgId == null) {
                throw new AccessDeniedException("Gestor sem organização vinculada.");
            }
            if (orgId != null && !orgId.equals(managerOrgId)) {
                throw new AccessDeniedException("O gestor só tem acesso às comunidades da sua própria organização.");
            }
            return communityRepository.findAllByOrganizationId(managerOrgId);
        }

        if (orgId != null) {
            return communityRepository.findAllByOrganizationId(orgId);
        }
        return communityRepository.findAll();
    }
}
