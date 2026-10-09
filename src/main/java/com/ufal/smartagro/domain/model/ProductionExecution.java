package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
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
    /** Momento em que a colheita apontada ocorreu. */
    private LocalDateTime harvestedAt;
    /** Estado da revisão, responsável, momento e observações do validador. */
    private ExecutionStatus status;
    private String validatedBy;
    private LocalDateTime validatedAt;
    private String notes;
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

    /** Construtor legado que adapta rendimento e data antiga para os novos campos. */
    public ProductionExecution(UUID id, ProductionPlan productionPlan, BigDecimal actualYield, java.time.LocalDate harvestDate,
                               BigDecimal latitude, BigDecimal longitude, BigDecimal locationAccuracy,
                               LocalDateTime locationRecordedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                               LocalDateTime deletedAt) {
        this(id, productionPlan, actualYield, actualYield,
                harvestDate == null ? null : harvestDate.atStartOfDay(), ExecutionStatus.PENDING,
                null, null, null, latitude, longitude, locationAccuracy, locationRecordedAt,
                createdAt, updatedAt, deletedAt);
    }

    /** Acesso de compatibilidade para consumidores Java antigos. */
    public BigDecimal getActualYield() { return quantityKg; }
    /** Acesso de compatibilidade; a data é derivada do novo timestamp da colheita. */
    public java.time.LocalDate getHarvestDate() { return harvestedAt == null ? null : harvestedAt.toLocalDate(); }
}
