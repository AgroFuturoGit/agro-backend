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

        String localName = dto.localName() != null ? dto.localName() : existingFarmer.getLocalName();

        Farmer updatedFarmer = Farmer.builder()
                .id(existingFarmer.getId())
                .user(existingFarmer.getUser())
                .community(existingFarmer.getCommunity())
                .fullName(dto.fullName() != null ? dto.fullName() : existingFarmer.getFullName())
                .cpf(existingFarmer.getCpf())
                .dateOfBirth(dto.dateOfBirth() != null ? dto.dateOfBirth() : existingFarmer.getDateOfBirth())
                .motherName(dto.motherName() != null ? dto.motherName() : existingFarmer.getMotherName())
                .origin(dto.origin() != null ? dto.origin() : existingFarmer.getOrigin())
                .educationLevel(dto.educationLevel() != null ? dto.educationLevel() : existingFarmer.getEducationLevel())
                .phone(dto.phone() != null ? dto.phone() : existingFarmer.getPhone())
                .localName(localName)
                .street(dto.street() != null ? dto.street() : existingFarmer.getStreet())
                .city(dto.city() != null ? dto.city() : existingFarmer.getCity())
                .state(dto.state() != null ? dto.state() : existingFarmer.getState())
                .ibgeCode(dto.ibgeCode() != null ? dto.ibgeCode() : existingFarmer.getIbgeCode())
                .latitude(dto.latitude() != null ? dto.latitude() : existingFarmer.getLatitude())
                .longitude(dto.longitude() != null ? dto.longitude() : existingFarmer.getLongitude())
                .registrationSource(existingFarmer.getRegistrationSource())
                .createdBy(existingFarmer.getCreatedBy())
                .isCompliant(dto.isCompliant() != null ? dto.isCompliant() : existingFarmer.getIsCompliant())
                .createdAt(existingFarmer.getCreatedAt())
                .updatedAt(null)
                .deletedAt(existingFarmer.getDeletedAt())
                .build();

        return farmerRepository.save(updatedFarmer);
    }
}
