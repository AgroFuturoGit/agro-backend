package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindCommunityByIdUseCase {

    private final CommunityRepository communityRepository;

    @Transactional(readOnly = true)
    public Community findById(UUID id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada."));
    }
}
