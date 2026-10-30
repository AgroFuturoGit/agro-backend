package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFarmerUseCase {

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID id) {
        delete(id, null);
    }

    @Transactional
    public void delete(UUID id, User loggedUser) {
        Farmer existingFarmer = farmerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agricultor não encontrado."));

        if (loggedUser != null) {
            accessValidator.validateAccess(existingFarmer, loggedUser);
        }

        farmerRepository.delete(id);

        if (existingFarmer.getUser() != null && existingFarmer.getUser().getId() != null) {
            userRepository.softDelete(existingFarmer.getUser().getId());
        }

        log.info("Produtor excluído logicamente [farmerId={}, linkedUserId={}] pelo usuário [actorId={}]",
                id,
                existingFarmer.getUser() != null ? existingFarmer.getUser().getId() : null,
                loggedUser != null ? loggedUser.getId() : "system");
    }
}
