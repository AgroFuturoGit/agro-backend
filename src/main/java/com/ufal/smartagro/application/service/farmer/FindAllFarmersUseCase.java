package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllFarmersUseCase {

    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public List<Farmer> findAll(java.util.UUID communityId) {
        if (communityId != null) {
            return farmerRepository.findAllByCommunityId(communityId);
        }
        return farmerRepository.findAll();
    }
}
