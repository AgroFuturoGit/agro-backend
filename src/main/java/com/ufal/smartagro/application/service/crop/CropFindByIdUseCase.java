package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CropFindByIdUseCase {

    private final CropRepository cropRepository;

    @Transactional(readOnly = true)
    public Crop execute(UUID id, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN
                && loggedUser.getRole() != Role.TECHNICIAN
                && loggedUser.getRole() != Role.MANAGER
                && loggedUser.getRole() != Role.FARMER) {
            throw new AccessDeniedException("Usuário não tem permissão para visualizar uma cultura.");
        }
        return cropRepository.findById(id)
                .orElseThrow(CropNotFoundException::new);
    }
}
