package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class HarvestFindByIdUseCase {

    private final HarvestRepository harvestRepository;

    @Transactional(readOnly = true)
    public Harvest execute(UUID id, User loggedUser) {
        return harvestRepository.findById(id)
                .orElseThrow(HarvestNotFoundException::new);
    }
}
