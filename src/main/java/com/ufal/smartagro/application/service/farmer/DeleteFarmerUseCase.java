package com.ufal.smartagro.application.service.farmer;

import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteFarmerUseCase {

    private final FarmerRepository farmerRepository;

    @Transactional
    public void delete(UUID id) {
        farmerRepository.findById(id)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Agricultor não encontrado."));

        farmerRepository.delete(id);
    }
}
