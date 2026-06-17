package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllCommunitiesUseCase {

    private final CommunityRepository communityRepository;

    @Transactional(readOnly = true)
    public List<Community> findAll() {
        return communityRepository.findAll();
    }
}
