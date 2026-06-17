package com.ufal.smartagro.application.service.producer;

import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindAllProducersUseCase {

    private final ProducerRepository producerRepository;

    @Transactional(readOnly = true)
    public List<Producer> findAll() {
        return producerRepository.findAll();
    }
}
