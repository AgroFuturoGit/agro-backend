package com.ufal.smartagro.application.service.technician;

import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.port.out.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindTechnicianByUserUseCase {

    private final TechnicianRepository technicianRepository;

    @Transactional(readOnly = true)
    public Technician findByUserId(UUID userId) {
        return technicianRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Técnico associado a este usuário não encontrado."));
    }
}
