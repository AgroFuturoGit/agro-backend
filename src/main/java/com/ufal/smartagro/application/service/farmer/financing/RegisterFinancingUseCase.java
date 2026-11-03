package com.ufal.smartagro.application.service.farmer.financing;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingRegisterDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.ProductionAccessValidator;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import com.ufal.smartagro.domain.port.out.FinancingRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterFinancingUseCase {

    private final FinancingRepository financingRepository;
    private final FarmerRepository farmerRepository;
    private final ProductionAccessValidator accessValidator;
    private final AuditLogService auditLogService;

    @Transactional
    public Financing register(UUID farmerId, FinancingRegisterDTO dto, User loggedUser) {
        if (loggedUser == null || (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.TECHNICIAN && loggedUser.getRole() != Role.FARMER)) {
            throw new AccessDeniedException("Apenas administradores, técnicos assistentes ou o próprio agricultor podem cadastrar financiamentos.");
        }

        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new EntityNotFoundException("Agricultor não encontrado."));

        accessValidator.validateWriteAccess(farmer, loggedUser);

        Financing financing = Financing.builder()
                .farmer(farmer)
                .description(dto.description().trim())
                .status(dto.status())
                .amount(dto.amount())
                .build();

        Financing saved = financingRepository.save(financing);

        Map<String, Object> details = new HashMap<>();
        details.put("farmerId", farmerId.toString());
        details.put("description", saved.getDescription());
        if (saved.getStatus() != null) {
            details.put("status", saved.getStatus().name());
        }
        if (saved.getAmount() != null) {
            details.put("amount", saved.getAmount());
        }

        auditLogService.record(
                loggedUser,
                AuditAction.CREATE,
                "financings",
                saved.getId(),
                details
        );

        return saved;
    }
}
