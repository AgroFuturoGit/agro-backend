package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerUpdateDTO;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateFarmerUseCase {

    private final FarmerRepository farmerRepository;

    @Transactional
    public Farmer update(UUID id, FarmerUpdateDTO dto) {
        Farmer existingFarmer = farmerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));

        Farmer updatedFarmer = new Farmer(
                existingFarmer.getId(),
                existingFarmer.getUser(),
                existingFarmer.getCommunity(),
                dto.aliasName() != null ? dto.aliasName() : existingFarmer.getAliasName(),
                dto.isCompliant() != null ? dto.isCompliant() : existingFarmer.getIsCompliant(),
                existingFarmer.getCreatedAt(),
                null,
                existingFarmer.getDeletedAt()
        );

        return farmerRepository.save(updatedFarmer);
    }
}
