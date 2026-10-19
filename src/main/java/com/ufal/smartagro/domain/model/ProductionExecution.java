package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;

/** Apontamento individual de colheita, com quantidades, posição e trilha de validação. */
@Getter
@AllArgsConstructor
public class ProductionExecution {
    private UUID id;
    private ProductionPlan productionPlan;
    /** Quantidade conforme a unidade configurada para a cultura. */
    private BigDecimal quantity;
    /** Quantidade normalizada em quilogramas para consolidações. */
    private BigDecimal quantityKg;
    /** Data em que a colheita apontada ocorreu. */
    private LocalDate harvestedAt;
    /** Estado da revisão, responsável, momento e observações do validador. */
    private ExecutionStatus status;
    private User validatedBy;
    private LocalDateTime validatedAt;
    private String notes;
    /** Justificativa formal informada pelo revisor ao rejeitar o apontamento. */
    private String rejectionReason;
    /** Onde o apontamento foi feito. Ausente quando o GPS não respondeu. */
    private BigDecimal latitude;
    private BigDecimal longitude;
    /** Raio de erro da leitura, em metros. */
    private BigDecimal locationAccuracy;
    /** Quando o GPS obteve a posição — não quando o apontamento foi salvo. */
    private LocalDateTime locationRecordedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /** Construtor de compatibilidade para chamadas com 16 argumentos (rejectionReason = null). */
    public ProductionExecution(UUID id, ProductionPlan productionPlan, BigDecimal quantity, BigDecimal quantityKg,
                               LocalDate harvestedAt, ExecutionStatus status, User validatedBy, LocalDateTime validatedAt,
                               String notes, BigDecimal latitude, BigDecimal longitude, BigDecimal locationAccuracy,
                               LocalDateTime locationRecordedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                               LocalDateTime deletedAt) {
        this(id, productionPlan, quantity, quantityKg, harvestedAt, status, validatedBy, validatedAt, notes, null,
                latitude, longitude, locationAccuracy, locationRecordedAt, createdAt, updatedAt, deletedAt);
    }

    /** Construtor legado que adapta rendimento e data antiga para os novos campos. */
    public ProductionExecution(UUID id, ProductionPlan productionPlan, BigDecimal actualYield, java.time.LocalDate harvestDate,
                               BigDecimal latitude, BigDecimal longitude, BigDecimal locationAccuracy,
                               LocalDateTime locationRecordedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                               LocalDateTime deletedAt) {
        this(id, productionPlan, actualYield, actualYield,
                harvestDate, ExecutionStatus.PENDING,
                null, null, null, null, latitude, longitude, locationAccuracy, locationRecordedAt,
                createdAt, updatedAt, deletedAt);
    }

    /** Acesso de compatibilidade para consumidores Java antigos. */
    public BigDecimal getActualYield() { return quantityKg; }
    /** Acesso de compatibilidade para consumidores Java antigos. */
    public LocalDate getHarvestDate() { return harvestedAt; }
}
