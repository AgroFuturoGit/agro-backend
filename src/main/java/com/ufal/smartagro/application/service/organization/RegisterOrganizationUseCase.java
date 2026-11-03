package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Organization register(OrganizationRegisterDTO dto, User loggedUser) {
        if (loggedUser == null || loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Apenas administradores podem cadastrar organizações.");
        }

        if (organizationRepository.existsByTaxId(dto.taxId())) {
            throw new IllegalArgumentException("Já existe uma organização com este CNPJ/taxId");
        }

        Organization organization = new Organization(
                null,
                dto.name(),
                dto.taxId(),
                dto.type(),
                null,
                null,
                null
        );

        Organization saved = organizationRepository.save(organization);

        auditLogService.record(
                loggedUser,
                AuditAction.CREATE,
                "organizations",
                saved.getId(),
                Map.of(
                        "name", saved.getName(),
                        "taxId", saved.getTaxId(),
                        "type", saved.getType() != null ? saved.getType().name() : "N/A"
                )
        );

        return saved;
    }
}
