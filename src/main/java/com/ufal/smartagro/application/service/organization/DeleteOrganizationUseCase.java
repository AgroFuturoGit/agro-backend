package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteOrganizationUseCase {

    private final OrganizationRepository organizationRepository;

    @Transactional
    public void delete(UUID id) {
        organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organização não encontrada."));
        
        organizationRepository.delete(id);
    }
}
