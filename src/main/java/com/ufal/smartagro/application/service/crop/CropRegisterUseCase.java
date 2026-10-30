package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CropRegisterUseCase {

    private final CropRepository cropRepository;

    @Transactional
    public Crop execute(Crop crop, User loggedUser) {
        // Double-checking authorization at the business logic level
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Usuário não tem permissão para cadastrar culturas.");
        }

        String targetName = crop.getName() != null ? crop.getName().trim() : null;
        String targetVariety = crop.getVariety() != null ? crop.getVariety().trim() : null;

        if (cropRepository.existsByNameAndVariety(targetName, targetVariety)) {
            throw new CropAlreadyExistsException();
        }

        Crop cropToSave = new Crop(
                crop.getId(),
                targetName,
                targetVariety,
                crop.getIsPriority(),
                crop.getCycleDays(),
                crop.getExpectedProductivity(),
                crop.getHarvestType(),
                crop.getUnit(),
                crop.getUnitWeightKg()
        );

        return cropRepository.save(cropToSave);
    }
}
