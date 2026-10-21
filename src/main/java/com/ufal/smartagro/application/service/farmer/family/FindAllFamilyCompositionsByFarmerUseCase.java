package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllFamilyCompositionsByFarmerUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public List<FamilyComposition> findAll(UUID farmerId, User loggedUser) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));

        accessValidator.validateAccess(farmer, loggedUser);

        return familyCompositionRepository.findAllByFarmerId(farmerId);
    }
}
