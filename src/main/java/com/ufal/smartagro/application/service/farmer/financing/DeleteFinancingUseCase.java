package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteFinancingUseCase {

    private final FinancingRepository financingRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public void delete(UUID id, User loggedUser) {
        Financing existing = financingRepository.findById(id)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Financiamento não encontrado."));

        accessValidator.validateWriteAccess(existing.getFarmer(), loggedUser);

        financingRepository.delete(id);
    }
}
