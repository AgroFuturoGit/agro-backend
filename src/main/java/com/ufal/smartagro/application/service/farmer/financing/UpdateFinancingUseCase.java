package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingUpdateDTO;
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
public class UpdateFinancingUseCase {

    private final FinancingRepository financingRepository;
    private final ProductionAccessValidator accessValidator;

    @Transactional
    public Financing update(UUID id, FinancingUpdateDTO dto, User loggedUser) {
        Financing existing = financingRepository.findById(id)
                .orElseThrow(() -> new com.ufal.smartagro.domain.exception.EntityNotFoundException("Financiamento não encontrado."));

        accessValidator.validateWriteAccess(existing.getFarmer(), loggedUser);

        Financing updated = Financing.builder()
                .id(existing.getId())
                .farmer(existing.getFarmer())
                .description(dto.description() != null && !dto.description().isBlank() ? dto.description().trim() : existing.getDescription())
                .status(dto.status() != null ? dto.status() : existing.getStatus())
                .amount(dto.amount() != null ? dto.amount() : existing.getAmount())
                .createdAt(existing.getCreatedAt())
                .updatedAt(null)
                .deletedAt(existing.getDeletedAt())
                .build();

        return financingRepository.save(updated);
    }
}
