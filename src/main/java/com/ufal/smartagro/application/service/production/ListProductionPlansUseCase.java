package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListProductionPlansUseCase {

    private final ProductionPlanRepository productionPlanRepository;
    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public List<ProductionPlan> listByFarmer(UUID farmerId, User loggedUser) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));

        accessValidator.validateAccess(farmer, loggedUser);

        return productionPlanRepository.findAllByFarmerId(farmerId);
    }
}
