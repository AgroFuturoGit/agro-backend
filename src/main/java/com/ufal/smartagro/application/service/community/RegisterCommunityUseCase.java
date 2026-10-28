package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegisterCommunityUseCase {

    private final CommunityRepository communityRepository;
    private final OrganizationRepository organizationRepository;
    private final ManagerRepository managerRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Community register(UUID organizationId, CommunityRegisterDTO dto, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER)) {
            throw new AccessDeniedException();
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada"));

        if (loggedUser.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.findByUserId(loggedUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Gestor não encontrado."));
            UUID managerOrgId = manager.getOrganization() != null ? manager.getOrganization().getId() : null;
            if (managerOrgId == null || !managerOrgId.equals(organizationId)) {
                throw new AccessDeniedException("O gestor só pode cadastrar comunidades na sua própria organização.");
            }
        }

        Community community = new Community(
                null,
                dto.name(),
                organization,
                null,
                null,
                null
        );

        Community saved = communityRepository.save(community);

        Map<String, Object> details = new HashMap<>();
        details.put("name", saved.getName());
        if (organization.getId() != null) {
            details.put("organizationId", organization.getId().toString());
        }

        auditLogService.record(
                loggedUser,
                AuditAction.CREATE,
                "communities",
                saved.getId(),
                details
        );

        return saved;
    }
}
