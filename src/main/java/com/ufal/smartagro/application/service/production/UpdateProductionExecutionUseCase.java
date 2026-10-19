package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Atualiza quantidades, horário, validação e localização de um apontamento existente. */
@Service
@RequiredArgsConstructor
public class UpdateProductionExecutionUseCase {

    private final ProductionExecutionRepository productionExecutionRepository;
    private final ProductionAccessValidator accessValidator;
    private final ProductionVersionGuard versionGuard;

    @Transactional
    /** Mantém campos omitidos, valida acesso/versão e persiste as alterações recebidas. */
    public ProductionExecution update(UUID executionId, ProductionExecutionUpdateDTO dto, User loggedUser) {
        ProductionExecution existingExecution = productionExecutionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execução de produção não encontrada."));

        if (existingExecution.getProductionPlan() != null) {
            accessValidator.validateWriteAccess(existingExecution.getProductionPlan().getFarmer(), loggedUser);
        }

        if (loggedUser.getRole() == com.ufal.smartagro.domain.model.enums.Role.FARMER) {
            if (dto.status() != null && dto.status() != existingExecution.getStatus() && dto.status() != com.ufal.smartagro.domain.model.enums.ExecutionStatus.PENDING) {
                throw new com.ufal.smartagro.domain.exception.AccessDeniedException("O agricultor não tem permissão para alterar o status do apontamento.");
            }
        }

        Localizacao localizacao = resolverLocalizacao(dto, existingExecution);

        versionGuard.ensureUpToDate(
                dto.baseUpdatedAt(),
                existingExecution.getUpdatedAt(),
                existingExecution.getCreatedAt(),
                Mapper.toProductionExecutionResponseDTO(existingExecution),
                "O apontamento foi alterado por outro usuário depois que esta edição começou."
        );

        BigDecimal newQuantity = dto.quantity() != null ? dto.quantity() : existingExecution.getQuantity();
        com.ufal.smartagro.domain.model.Crop crop = existingExecution.getProductionPlan() != null
                ? existingExecution.getProductionPlan().getCrop() : null;
        BigDecimal fallbackKg = dto.quantityKg() != null ? dto.quantityKg() : existingExecution.getQuantityKg();
        BigDecimal newQuantityKg = ProductionQuantityCalculator.calculateQuantityKg(crop, newQuantity, fallbackKg);

        com.ufal.smartagro.domain.model.enums.ExecutionStatus newStatus = existingExecution.getStatus();
        User newValidatedBy = existingExecution.getValidatedBy();
        LocalDateTime newValidatedAt = existingExecution.getValidatedAt();

        boolean isQuantityOrDateChanged = (dto.quantity() != null && !dto.quantity().equals(existingExecution.getQuantity()))
                || (dto.quantityKg() != null && !dto.quantityKg().equals(existingExecution.getQuantityKg()))
                || (dto.harvestedAt() != null && !dto.harvestedAt().equals(existingExecution.getHarvestedAt()));

        if (loggedUser.getRole() == com.ufal.smartagro.domain.model.enums.Role.FARMER) {
            if (isQuantityOrDateChanged && existingExecution.getStatus() != com.ufal.smartagro.domain.model.enums.ExecutionStatus.PENDING) {
                newStatus = com.ufal.smartagro.domain.model.enums.ExecutionStatus.PENDING;
                newValidatedBy = null;
                newValidatedAt = null;
            }
        } else {
            if (dto.status() != null) {
                newStatus = dto.status();
                if (newStatus == com.ufal.smartagro.domain.model.enums.ExecutionStatus.VALIDATED
                        || newStatus == com.ufal.smartagro.domain.model.enums.ExecutionStatus.REJECTED) {
                    newValidatedBy = loggedUser;
                    newValidatedAt = LocalDateTime.now();
                } else if (newStatus == com.ufal.smartagro.domain.model.enums.ExecutionStatus.PENDING) {
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
        if (newStatus == com.ufal.smartagro.domain.model.enums.ExecutionStatus.VALIDATED
                || newStatus == com.ufal.smartagro.domain.model.enums.ExecutionStatus.PENDING) {
            newRejectionReason = null;
        }

        ProductionExecution updatedExecution = new ProductionExecution(
                existingExecution.getId(),
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

        return productionExecutionRepository.save(updatedExecution);
    }

    private User userReference(UUID userId) {
        return User.builder().id(userId).build();
    }

    private record Localizacao(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracy,
            LocalDateTime recordedAt
    ) {}

    /**
     * Decide o destino da localização numa edição, entre três intenções que um
     * campo nulo sozinho não distingue:
     *
     * <ul>
     *   <li>{@code clearLocation} — apagar a posição de propósito, para quando o
     *       usuário reconhece que ela não corresponde ao local da colheita;</li>
     *   <li>coordenada presente — recaptura: substitui tudo, inclusive precisão
     *       e carimbo, que pertencem àquela leitura e não podem ficar para trás;</li>
     *   <li>nada — preserva. Um formulário salvo sem GPS não pode apagar a
     *       posição registrada em campo.</li>
     * </ul>
     */
    /** Interpreta a intenção de manter, substituir ou limpar a posição GPS no apontamento. */
    private Localizacao resolverLocalizacao(
            ProductionExecutionUpdateDTO dto,
            ProductionExecution existente
    ) {
        if (Boolean.TRUE.equals(dto.clearLocation())) {
            return new Localizacao(null, null, null, null);
        }

        if (dto.latitude() != null && dto.longitude() != null) {
            return new Localizacao(
                    dto.latitude(),
                    dto.longitude(),
                    dto.locationAccuracy(),
                    dto.locationRecordedAt()
            );
        }

        return new Localizacao(
                existente.getLatitude(),
                existente.getLongitude(),
                existente.getLocationAccuracy(),
                existente.getLocationRecordedAt()
        );
    }
}
