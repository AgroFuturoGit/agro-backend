package com.ufal.smartagro.application.service.farmer;

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
public class FindFarmerByIdUseCase {

    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional(readOnly = true)
    public Farmer findById(UUID id) {
        return findById(id, null);
    }

    @Transactional(readOnly = true)
    public Farmer findById(UUID id, User loggedUser) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agricultor não encontrado."));

        if (loggedUser != null) {
            accessValidator.validateAccess(farmer, loggedUser);
        }

        return farmer;
    }
}
