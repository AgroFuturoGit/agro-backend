package com.ufal.smartagro.application.service.crop;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.CropRepository;
import com.ufal.smartagro.domain.port.out.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CropDeleteUseCase {

    private final CropRepository cropRepository;
    private final ProductionPlanRepository productionPlanRepository;

    public void execute(UUID id, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN) {
            throw new AccessDeniedException("Usuário não tem permissão para deletar uma cultura.");
        }

        cropRepository.findById(id)
                .orElseThrow(CropNotFoundException::new);

        long planCount = productionPlanRepository.countByCropId(id);
        if (planCount > 0 || productionPlanRepository.existsByCropId(id)) {
            long effectiveCount = planCount > 0 ? planCount : 1L;
            throw new EntityInUseException(
                    "Não é possível excluir uma cultura que possui " + effectiveCount + " planos de produção vinculados."
            );
        }

        cropRepository.deleteById(id);
        log.info("Cultura excluída logicamente [cropId={}] pelo usuário [actorId={}, actorRole={}]",
                id, loggedUser.getId(), loggedUser.getRole());
    }
}
