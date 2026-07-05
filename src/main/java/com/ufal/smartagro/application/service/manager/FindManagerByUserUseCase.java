package com.ufal.smartagro.application.service.manager;

import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.port.out.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindManagerByUserUseCase {

    private final ManagerRepository managerRepository;

    @Transactional(readOnly = true)
    public Manager findByUserId(UUID userId) {
        return managerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Gerente associado a este usuário não encontrado."));
    }
}
