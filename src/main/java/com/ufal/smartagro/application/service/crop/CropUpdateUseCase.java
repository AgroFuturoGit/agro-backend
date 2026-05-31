package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CropUpdateUseCase {

    private final CropRepository cropRepository;

    public Crop execute(UUID id, Crop updatedCropData, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("User does not have permission to update a crop.");
        }

        Crop existingCrop = cropRepository.findById(id)
                .orElseThrow(CropNotFoundException::new);

        boolean isNameOrVarietyChanged = !existingCrop.getName().equals(updatedCropData.getName()) ||
                !existingCrop.getVariety().equals(updatedCropData.getVariety());

        if (isNameOrVarietyChanged && cropRepository.existsByNameAndVariety(updatedCropData.getName(), updatedCropData.getVariety())) {
            throw new CropAlreadyExistsException();
        }

        Crop cropToSave = new Crop(
                existingCrop.getId(),
                updatedCropData.getName(),
                updatedCropData.getVariety(),
                updatedCropData.getIsPriority()
        );

        return cropRepository.save(cropToSave);
    }
}
