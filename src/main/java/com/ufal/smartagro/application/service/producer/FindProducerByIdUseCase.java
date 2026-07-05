package com.ufal.smartagro.application.service.producer;

import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindProducerByIdUseCase {

    private final ProducerRepository producerRepository;

    @Transactional(readOnly = true)
    public Producer findById(UUID id) {
        return producerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));
    }
}
