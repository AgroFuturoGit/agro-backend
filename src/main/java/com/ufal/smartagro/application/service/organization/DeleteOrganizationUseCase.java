package com.ufal.smartagro.application.service.organization;

import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final CommunityRepository communityRepository;

    @Transactional
    public void delete(UUID id) {
        organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organização não encontrada."));

        int communityCount = communityRepository.findAllByOrganizationId(id).size();
        if (communityCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir uma organização que possui " + communityCount + " comunidades vinculadas."
            );
        }

        organizationRepository.delete(id);
    }
}
