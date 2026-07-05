package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationUpdateDTO;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateOrganizationUseCase {

    private final OrganizationRepository organizationRepository;

    @Transactional
    public Organization update(UUID id, OrganizationUpdateDTO dto) {
        Organization existingOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organização não encontrada."));

        Organization updatedOrganization = new Organization(
                existingOrganization.getId(),
                dto.name() != null ? dto.name() : existingOrganization.getName(),
                existingOrganization.getTaxId(),
                dto.type() != null ? dto.type() : existingOrganization.getType(),
                existingOrganization.getCreatedAt(),
                null,
                existingOrganization.getDeletedAt()
        );

        return organizationRepository.save(updatedOrganization);
    }
}
