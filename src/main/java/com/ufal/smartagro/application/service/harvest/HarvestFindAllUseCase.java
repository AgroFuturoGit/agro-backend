package com.ufal.smartagro.application.service.harvest;

import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.HarvestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class HarvestFindAllUseCase {

    private final HarvestRepository harvestRepository;

    @Transactional(readOnly = true)
    public List<Harvest> execute(User loggedUser) {
        if (loggedUser == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return harvestRepository.findAll();
    }
}
