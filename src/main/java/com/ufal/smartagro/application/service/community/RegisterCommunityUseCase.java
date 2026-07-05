package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityRegisterDTO;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegisterCommunityUseCase {

    private final CommunityRepository communityRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Community register(UUID organizationId, CommunityRegisterDTO dto, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException();
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organização não encontrada"));

        Community community = new Community(
                null,
                dto.name(),
                organization,
                null,
                null,
                null
        );

        return communityRepository.save(community);
    }
}
