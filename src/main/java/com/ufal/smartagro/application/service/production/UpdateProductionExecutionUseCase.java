package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.exception.InvalidStateTransitionException;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Atualiza quantidades, horário, validação e localização de um apontamento existente. */
@Service
@RequiredArgsConstructor
public class UpdateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;
    private final ProductionVersionGuard versionGuard;
    private final AuditLogService auditLogService;

    @Transactional
    /** Mantém campos omitidos, valida acesso/versão e persiste as alterações recebidas. */
    public ProductionExecution update(UUID executionId, ProductionExecutionUpdateDTO dto, User loggedUser) {
        ProductionExecution existingExecution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de produção não encontrada."));

        if (existingExecution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(existingExecution.getProductionPlan().getFarmer(), loggedUser);
        }

        if (dto.harvestedAt() != null) {
            if (dto.harvestedAt().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("A data da colheita não pode ser futura.");
            }
            if (dto.harvestedAt().isBefore(LocalDate.of(1970, 1, 1))) {
                throw new IllegalArgumentException("A data da colheita não pode ser anterior a 1970.");
            }
            if (existingExecution.getProductionPlan() != null) {
                ProductionPlan plan = existingExecution.getProductionPlan();
                if (plan.getPlannedPlantingDate() != null && dto.harvestedAt().isBefore(plan.getPlannedPlantingDate())) {
                    throw new IllegalArgumentException("A data da colheita não pode ser anterior à data planejada de plantio.");
                }
                Harvest harvest = plan.getHarvest();
                if (harvest != null && harvest.getStartDate() != null && dto.harvestedAt().isBefore(harvest.getStartDate())) {
                    throw new IllegalArgumentException("A data da colheita não pode ser anterior ao início da safra.");
                }
            }
        }

        if (loggedUser.getRole() == Role.FARMER) {
            if (dto.status() != null && dto.status() != existingExecution.getStatus() && dto.status() != ExecutionStatus.PENDING) {
                throw new AccessDeniedException("O agricultor não tem permissão para alterar o status do apontamento.");
            }
        }

        Localizacao localizacao = resolverLocalizacao(dto, existingExecution);

        versionGuard.ensureUpToDate(
                dto.baseUpdatedAt(),
                existingExecution.getUpdatedAt(),
                existingExecution.getCreatedAt(),
                existingExecution,
                "O apontamento foi alterado por outro usuário depois que esta edição começou."
        );

        BigDecimal newQuantity = dto.quantity() != null ? dto.quantity() : existingExecution.getQuantity();
        Crop crop = existingExecution.getProductionPlan() != null
                ? existingExecution.getProductionPlan().getCrop() : null;
        BigDecimal fallbackKg = dto.quantityKg() != null ? dto.quantityKg() : existingExecution.getQuantityKg();
        BigDecimal newQuantityKg = ProductionQuantityCalculator.calculateQuantityKg(crop, newQuantity, fallbackKg);

        ExecutionStatus newStatus = existingExecution.getStatus();
        User newValidatedBy = existingExecution.getValidatedBy();
        LocalDateTime newValidatedAt = existingExecution.getValidatedAt();

        boolean isQuantityOrDateChanged = (dto.quantity() != null && !dto.quantity().equals(existingExecution.getQuantity()))
                || (dto.quantityKg() != null && !dto.quantityKg().equals(existingExecution.getQuantityKg()))
                || (dto.harvestedAt() != null && !dto.harvestedAt().equals(existingExecution.getHarvestedAt()));

        if (loggedUser.getRole() == Role.FARMER) {
            if (isQuantityOrDateChanged && existingExecution.getStatus() != ExecutionStatus.PENDING) {
                newStatus = ExecutionStatus.PENDING;
                newValidatedBy = null;
                newValidatedAt = null;
            }
        } else {
            if (dto.status() != null && dto.status() != existingExecution.getStatus()) {
                if (dto.status() == ExecutionStatus.VALIDATED) {
                    if (existingExecution.getStatus() != ExecutionStatus.PENDING) {
                        throw new InvalidStateTransitionException(
                                "Apenas apontamentos com status PENDENTE podem ser validados. Status atual: " + existingExecution.getStatus()
                        );
                    }
                    newStatus = ExecutionStatus.VALIDATED;
                    newValidatedBy = loggedUser;
                    newValidatedAt = LocalDateTime.now();
                } else if (dto.status() == ExecutionStatus.REJECTED) {
                    if (existingExecution.getStatus() != ExecutionStatus.PENDING) {
                        throw new InvalidStateTransitionException(
                                "Apenas apontamentos com status PENDENTE podem ser rejeitados. Status atual: " + existingExecution.getStatus()
                        );
                    }
                    newStatus = ExecutionStatus.REJECTED;
                    newValidatedBy = loggedUser;
                    newValidatedAt = LocalDateTime.now();
                } else if (dto.status() == ExecutionStatus.PENDING) {
                    newStatus = ExecutionStatus.PENDING;
                    newValidatedBy = null;
                    newValidatedAt = null;
                }
            }
        }

        String newNotes = existingExecution.getNotes();
        if (dto.notes() != null) {
            newNotes = dto.notes().trim().isEmpty() ? null : dto.notes();
        }

        String newRejectionReason = existingExecution.getRejectionReason();
        if (newStatus == ExecutionStatus.VALIDATED || newStatus == ExecutionStatus.PENDING) {
            newRejectionReason = null;
        } else if (newStatus == ExecutionStatus.REJECTED && newRejectionReason == null) {
            newRejectionReason = (dto.notes() != null && !dto.notes().trim().isEmpty())
                    ? dto.notes().trim()
                    : "Rejeitado via atualização";
        }

        ProductionExecution updatedExecution = new ProductionExecution(
                existingExecution.getId(),
                existingExecution.getClientId(),
                existingExecution.getProductionPlan(),
                newQuantity,
                newQuantityKg,
                dto.harvestedAt() != null ? dto.harvestedAt() : existingExecution.getHarvestedAt(),
                newStatus,
                newValidatedBy,
                newValidatedAt,
                newNotes,
                newRejectionReason,
                localizacao.latitude(),
                localizacao.longitude(),
                localizacao.accuracy(),
                localizacao.recordedAt(),
                existingExecution.getCreatedAt(),
                null,
                existingExecution.getDeletedAt()
        );

        ProductionExecution saved = productionExecutionRepository.save(updatedExecution);

        if (existingExecution.getStatus() != newStatus) {
            if (newStatus == ExecutionStatus.VALIDATED) {
                Map<String, Object> details = new HashMap<>();
                details.put("previousStatus", existingExecution.getStatus() != null ? existingExecution.getStatus().name() : null);
                details.put("quantityKg", saved.getQuantityKg());
                auditLogService.record(loggedUser, AuditAction.VALIDATE, "production_executions", saved.getId(), details);
            } else if (newStatus == ExecutionStatus.REJECTED) {
                Map<String, Object> details = new HashMap<>();
                details.put("previousStatus", existingExecution.getStatus() != null ? existingExecution.getStatus().name() : null);
                details.put("reason", saved.getRejectionReason());
                auditLogService.record(loggedUser, AuditAction.REJECT, "production_executions", saved.getId(), details);
            }
        }

        return saved;
    }

    private Localizacao resolverLocalizacao(ProductionExecutionUpdateDTO dto, ProductionExecution existing) {
        if (Boolean.TRUE.equals(dto.clearLocation())) {
            return new Localizacao(null, null, null, null);
        }
        BigDecimal lat = dto.latitude() != null ? dto.latitude() : existing.getLatitude();
        BigDecimal lon = dto.longitude() != null ? dto.longitude() : existing.getLongitude();
        BigDecimal acc = dto.locationAccuracy() != null ? dto.locationAccuracy() : existing.getLocationAccuracy();
        LocalDateTime at = dto.locationRecordedAt() != null ? dto.locationRecordedAt() : existing.getLocationRecordedAt();
        return new Localizacao(lat, lon, acc, at);
    }

    private record Localizacao(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt) {}
}
