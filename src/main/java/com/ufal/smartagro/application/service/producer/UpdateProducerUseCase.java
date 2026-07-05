package com.ufal.smartagro.application.service.producer;

import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerUpdateDTO;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProducerUseCase {

    private final ProducerRepository producerRepository;

    @Transactional
    public Producer update(UUID id, ProducerUpdateDTO dto) {
        Producer existingProducer = producerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produtor não encontrado."));

        Producer updatedProducer = new Producer(
                existingProducer.getId(),
                existingProducer.getUser(),
                existingProducer.getCommunity(),
                dto.aliasName() != null ? dto.aliasName() : existingProducer.getAliasName(),
                dto.isCompliant() != null ? dto.isCompliant() : existingProducer.getIsCompliant(),
                existingProducer.getCreatedAt(),
                null,
                existingProducer.getDeletedAt()
        );

        return producerRepository.save(updatedProducer);
    }
}
