package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindOrganizationByIdUseCase {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public Organization findById(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organização não encontrada."));
    }
}
