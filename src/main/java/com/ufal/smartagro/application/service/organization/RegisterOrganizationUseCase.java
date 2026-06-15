package com .ufal.smartagro.application.service.organization;

import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationResponseDTO;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationUseCase {

    private final OrganizationRepository organizationRepository;

    @Transactional
    public OrganizationResponseDTO register(OrganizationRegisterDTO dto, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException();
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

        return new OrganizationResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getTaxId(),
                saved.getType(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
