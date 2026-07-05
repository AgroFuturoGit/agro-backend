package com.ufal.smartagro.application.service.producer;

import com.ufal.smartagro.domain.port.out.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteProducerUseCase {

    private final ProducerRepository producerRepository;

    @Transactional
    public void delete(UUID id) {
        producerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));

        producerRepository.delete(id);
    }
}
