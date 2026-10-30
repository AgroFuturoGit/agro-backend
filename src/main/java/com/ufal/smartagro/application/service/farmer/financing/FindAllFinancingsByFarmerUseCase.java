package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllFinancingsByFarmerUseCase {

    private final FinancingRepository financingRepository;
    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public List<Financing> findAll(UUID farmerId, User loggedUser) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Agricultor não encontrado."));

        accessValidator.validateAccess(farmer, loggedUser);

        return financingRepository.findAllByFarmerId(farmerId);
    }
}
