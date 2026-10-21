package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CropDeleteUseCase {

    private final CropRepository cropRepository;
    private final ProductionPlanRepository productionPlanRepository;

    public void execute(UUID id, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("User does not have permission to delete a crop.");
        }

        if (productionPlanRepository.existsByCropId(id)) {
            throw new EntityInUseException("Não é possível excluir uma cultura que possui planos de produção vinculados.");
        }

        cropRepository.deleteById(id);
    }
}
