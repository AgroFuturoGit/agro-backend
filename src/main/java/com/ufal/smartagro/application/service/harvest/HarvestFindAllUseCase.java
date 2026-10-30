package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HarvestFindAllUseCase {

    private final HarvestRepository harvestRepository;

    @Transactional(readOnly = true)
    public List<Harvest> execute(User loggedUser) {
        return harvestRepository.findAll();
    }
}
