package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CropRegisterUseCase {

    private final CropRepository cropRepository;

    public Crop execute(Crop crop, User loggedUser) {
        // Double-checking authorization at the business logic level
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("User does not have permission to register a crop.");
        }

        if (cropRepository.existsByNameAndVariety(crop.getName(), crop.getVariety())) {
            throw new CropAlreadyExistsException();
        }
        return cropRepository.save(crop);
    }
}
