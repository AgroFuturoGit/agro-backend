package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindOrganizationByIdUseCase {

    private final OrganizationRepository organizationRepository;
    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public Organization findById(UUID id) {
        return findById(id, null);
    }

    @Transactional(readOnly = true)
    public Organization findById(UUID id, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Apenas administradores e gestores podem consultar os dados da organização.");
        }

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));

        if (loggedUser.getRole() == Role.ADMIN) {
            return organization;
        } else if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));

            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            if (managerOrgId == null || !managerOrgId.equals(id)) {
                throw new AccessDeniedException("O gestor só tem acesso à sua própria organização.");
            }
            return organization;
        }

        throw new AccessDeniedException("Apenas administradores e gestores podem consultar os dados da organização.");
    }
}
