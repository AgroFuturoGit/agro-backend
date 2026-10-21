package com.ufal.smartagro.application.service.farmer.family;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionRegisterDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FamilyCompositionRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterFamilyCompositionUseCase {

    private final FamilyCompositionRepository familyCompositionRepository;
    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public FamilyComposition register(UUID farmerId, FamilyCompositionRegisterDTO dto, User loggedUser) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));

        accessValidator.validateWriteAccess(farmer, loggedUser);

        FamilyComposition composition = FamilyComposition.builder()
                .farmer(farmer)
                .name(dto.name().trim())
                .kinship(dto.kinship().trim())
                .age(dto.age())
                .build();

        return familyCompositionRepository.save(composition);
    }
}
