package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFinancingUseCase {

    private final FinancingRepository financingRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public Financing update(UUID id, FinancingUpdateDTO dto, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN && loggedUser.getRole() != Role.FARMER)) {
            throw new AccessDeniedException("Apenas administradores, técnicos assistentes ou o próprio agricultor podem atualizar financiamentos.");
        }

        Financing existing = financingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Financiamento não encontrado."));

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

        Financing saved = financingRepository.save(updated);

        Map<String, Object> details = new HashMap<>();
        details.put("farmerId", saved.getFarmer().getId().toString());
        details.put("description", saved.getDescription());
        if (saved.getStatus() != null) {
            details.put("status", saved.getStatus().name());
        }
        if (saved.getAmount() != null) {
            details.put("amount", saved.getAmount());
        }

        auditLogService.record(
                loggedUser,
                AuditAction.UPDATE,
                "financings",
                saved.getId(),
                details
        );

        return saved;
    }
}
