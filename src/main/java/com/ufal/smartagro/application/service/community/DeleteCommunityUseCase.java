package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.port.out.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCommunityUseCase {

    private final CommunityRepository communityRepository;

    @Transactional
    public void delete(UUID id) {
        communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada."));
        
        communityRepository.delete(id);
    }
}
