package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.CropRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CropFindAllUseCase {

    private final CropRepository cropRepository;

    @Transactional(readOnly = true)
    public List<Crop> execute(User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return cropRepository.findAll();
    }
}
