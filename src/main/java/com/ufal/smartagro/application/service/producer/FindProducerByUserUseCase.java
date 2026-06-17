package com.ufal.smartagro.application.service.producer;

import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindProducerByUserUseCase {

    private final ProducerRepository producerRepository;

    @Transactional(readOnly = true)
    public Producer findByUserId(UUID userId) {
        return producerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Produtor associado a este usuário não encontrado."));
    }
}
