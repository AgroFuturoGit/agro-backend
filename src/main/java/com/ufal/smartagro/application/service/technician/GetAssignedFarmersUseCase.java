package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAssignedFarmersUseCase {

    private final GetAssignedCommunitiesUseCase getAssignedCommunitiesUseCase;
    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public List<Farmer> getAssignedFarmers(UUID technicianId, User loggedUser) {
        List<Community> communities = getAssignedCommunitiesUseCase.getAssignedCommunities(technicianId, loggedUser);
        return communities.stream()
                .flatMap(c -> farmerRepository.findAllByCommunityId(c.getId()).stream())
                .filter(f -> f.getDeletedAt() == null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Farmer> getAssignedFarmers(User loggedUser) {
        List<Community> communities = getAssignedCommunitiesUseCase.getAssignedCommunities(loggedUser);
        return communities.stream()
                .flatMap(c -> farmerRepository.findAllByCommunityId(c.getId()).stream())
                .filter(f -> f.getDeletedAt() == null)
                .collect(Collectors.toList());
    }
}
