package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerUpdateDTO;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateFarmerUseCase {

    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public Farmer update(UUID id, FarmerUpdateDTO dto) {
        return update(id, dto, null);
    }

    @Transactional
    public Farmer update(UUID id, FarmerUpdateDTO dto, User loggedUser) {
        Farmer existingFarmer = farmerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agricultor não encontrado."));

        if (loggedUser != null) {
            accessValidator.validateAccess(existingFarmer, loggedUser);
        }

        String fullName = existingFarmer.getFullName();
        if (dto.fullName() != null) {
            String trimmed = dto.fullName().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("O nome completo não pode ser vazio.");
            }
            fullName = trimmed;
        }

        String localName = existingFarmer.getLocalName();
        if (dto.localName() != null) {
            String trimmed = dto.localName().trim();
            localName = trimmed.isEmpty() ? null : trimmed;
        }

        String motherName = existingFarmer.getMotherName();
        if (dto.motherName() != null) {
            String trimmed = dto.motherName().trim();
            motherName = trimmed.isEmpty() ? null : trimmed;
        }

        String origin = existingFarmer.getOrigin();
        if (dto.origin() != null) {
            String trimmed = dto.origin().trim();
            origin = trimmed.isEmpty() ? null : trimmed;
        }

        String educationLevel = existingFarmer.getEducationLevel();
        if (dto.educationLevel() != null) {
            String trimmed = dto.educationLevel().trim();
            educationLevel = trimmed.isEmpty() ? null : trimmed;
        }

        String phone = existingFarmer.getPhone();
        if (dto.phone() != null) {
            String trimmed = dto.phone().trim();
            phone = trimmed.isEmpty() ? null : trimmed;
        }

        String street = existingFarmer.getStreet();
        if (dto.street() != null) {
            String trimmed = dto.street().trim();
            street = trimmed.isEmpty() ? null : trimmed;
        }

        String city = existingFarmer.getCity();
        if (dto.city() != null) {
            String trimmed = dto.city().trim();
            city = trimmed.isEmpty() ? null : trimmed;
        }

        String state = existingFarmer.getState();
        if (dto.state() != null) {
            String trimmed = dto.state().trim();
            state = trimmed.isEmpty() ? null : trimmed;
        }

        String ibgeCode = existingFarmer.getIbgeCode();
        if (dto.ibgeCode() != null) {
            String trimmed = dto.ibgeCode().trim();
            ibgeCode = trimmed.isEmpty() ? null : trimmed;
        }

        Farmer updatedFarmer = Farmer.builder()
                .id(existingFarmer.getId())
                .user(existingFarmer.getUser())
                .community(existingFarmer.getCommunity())
                .fullName(fullName)
                .cpf(existingFarmer.getCpf())
                .dateOfBirth(dto.dateOfBirth() != null ? dto.dateOfBirth() : existingFarmer.getDateOfBirth())
                .motherName(motherName)
                .origin(origin)
                .educationLevel(educationLevel)
                .phone(phone)
                .localName(localName)
                .street(street)
                .city(city)
                .state(state)
                .ibgeCode(ibgeCode)
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
