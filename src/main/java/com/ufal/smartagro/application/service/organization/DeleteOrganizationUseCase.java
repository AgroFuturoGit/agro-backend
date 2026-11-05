package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
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

@Service
@RequiredArgsConstructor
public class DeleteOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final CommunityRepository communityRepository;
    private final ManagerRepository managerRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void delete(UUID id) {
        delete(id, null);
    }

    @Transactional
    public void delete(UUID id, User loggedUser) {
        if (loggedUser != null && loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem excluir organizações.");
        }

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));

        long communityCount = communityRepository.countByOrganizationId(id);
        if (communityCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir uma organização que possui " + communityCount + " comunidades vinculadas."
            );
        }

        long managerCount = managerRepository.countByOrganizationId(id);
        if (managerCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir uma organização que possui " + managerCount + " gestores vinculados."
            );
        }

        organizationRepository.delete(id);

        if (loggedUser != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("name", organization.getName());
            if (organization.getTaxId() != null) {
                details.put("taxId", organization.getTaxId());
            }
            auditLogService.record(
                    loggedUser,
                    AuditAction.DELETE,
                    "organizations",
                    id,
                    details
            );
        }
    }
}
