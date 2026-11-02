package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CropRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CropFindByIdUseCase {

    private final CropRepository cropRepository;

    @Transactional(readOnly = true)
    public Crop execute(UUID id, User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return cropRepository.findById(id)
                .orElseThrow(CropNotFoundException::new);
    }
}
