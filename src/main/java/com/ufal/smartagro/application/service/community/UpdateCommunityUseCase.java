package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityUpdateDTO;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCommunityUseCase {

    private final CommunityRepository communityRepository;

    @Transactional
    public Community update(UUID id, CommunityUpdateDTO dto) {
        Community existingCommunity = communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada."));

        Community updatedCommunity = new Community(
                existingCommunity.getId(),
                dto.name(),
                existingCommunity.getOrganization(),
                existingCommunity.getCreatedAt(),
                null,
                existingCommunity.getDeletedAt()
        );

        return communityRepository.save(updatedCommunity);
    }
}
