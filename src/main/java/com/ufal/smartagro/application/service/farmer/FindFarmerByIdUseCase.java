package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindFarmerByIdUseCase {

    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public Farmer findById(UUID id) {
        return farmerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agricultor não encontrado."));
    }
}
