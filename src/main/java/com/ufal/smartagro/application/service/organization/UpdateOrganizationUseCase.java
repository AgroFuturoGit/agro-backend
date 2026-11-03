package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Organization update(UUID id, OrganizationUpdateDTO dto) {
        return update(id, dto, null);
    }

    @Transactional
    public Organization update(UUID id, OrganizationUpdateDTO dto, User loggedUser) {
        if (loggedUser != null && loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem atualizar organizações.");
        }

        Organization existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));

        String targetName = existingOrganization.getName();
        if (dto.name() != null) {
            String trimmed = dto.name().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("O nome da organização não pode ser vazio.");
            }
            targetName = trimmed;
        }

        Organization updatedOrganization = new Organization(
                existingOrganization.getId(),
                targetName,
                existingOrganization.getTaxId(),
                dto.type() != null ? dto.type() : existingOrganization.getType(),
                existingOrganization.getCreatedAt(),
                LocalDateTime.now(),
                existingOrganization.getDeletedAt()
        );

        Organization saved = organizationRepository.save(updatedOrganization);

        if (loggedUser != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("name", saved.getName());
            if (saved.getType() != null) {
                details.put("type", saved.getType().name());
            }
            auditLogService.record(
                    loggedUser,
                    AuditAction.UPDATE,
                    "organizations",
                    saved.getId(),
                    details
            );
        }

        return saved;
    }
}
