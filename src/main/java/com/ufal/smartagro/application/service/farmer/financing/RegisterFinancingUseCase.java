package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingRegisterDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterFinancingUseCase {

    private final FinancingRepository financingRepository;
    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public Financing register(UUID farmerId, FinancingRegisterDTO dto, User loggedUser) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));

        accessValidator.validateWriteAccess(farmer, loggedUser);

        Financing financing = Financing.builder()
                .farmer(farmer)
                .description(dto.description().trim())
                .status(dto.status())
                .amount(dto.amount())
                .build();

        return financingRepository.save(financing);
    }
}
