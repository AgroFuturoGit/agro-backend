package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindFarmerByUserUseCase {

    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public Farmer findByUserId(UUID userId) {
        return farmerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor associado a este usuário não encontrado."));
    }
}
