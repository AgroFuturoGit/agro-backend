package com.ufal.smartagro.adapters.in.web.dto.production;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Dados de um apontamento de colheita; o estado inicial de validação é definido pelo serviço. */
public record ProductionExecutionRegisterDTO(
        @Schema(description = "Identificador gerado no cliente offline para deduplicação idempotente", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID clientId,

        @NotNull(message = "A quantidade colhida é obrigatória.")
        @DecimalMin(value = "0.01", message = "A quantidade produzida deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "A quantidade deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal quantity,

        @NotNull(message = "A quantidade em kg é obrigatória.")
        @DecimalMin(value = "0.01", message = "A quantidade em kg deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "A quantidade em kg deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal quantityKg,

        @NotNull(message = "A data da colheita é obrigatória.")
        @PastOrPresent(message = "A data da colheita não pode estar no futuro.")
        LocalDate harvestedAt,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.")
        String notes,

        /**
         * Onde o apontamento foi feito. Opcional: o aparelho pode estar sem
         * sinal de GPS ou com a permissão negada, e isso não pode impedir o
         * registro da produção.
         */
        @DecimalMin(value = "-90.0", message = "Latitude inválida.")
        @DecimalMax(value = "90.0", message = "Latitude inválida.")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude inválida.")
        @DecimalMax(value = "180.0", message = "Longitude inválida.")
        BigDecimal longitude,

        /** Raio de erro da leitura, em metros, como informado pelo GPS. */
        @DecimalMin(value = "0.0", message = "A precisão não pode ser negativa.")
        BigDecimal locationAccuracy,

        /** Quando o GPS obteve a posição. */
        LocalDateTime locationRecordedAt
) {
    /** Construtor de compatibilidade para chamadas sem clientId. */
    public ProductionExecutionRegisterDTO(BigDecimal quantity, BigDecimal quantityKg, LocalDate harvestedAt,
                                          String notes, BigDecimal latitude, BigDecimal longitude,
                                          BigDecimal locationAccuracy, LocalDateTime locationRecordedAt) {
        this(null, quantity, quantityKg, harvestedAt, notes, latitude, longitude, locationAccuracy, locationRecordedAt);
    }

    /** Converte a assinatura antiga para os novos campos mantendo a data da colheita. */
    public ProductionExecutionRegisterDTO(BigDecimal actualYield, LocalDate harvestDate, BigDecimal latitude,
                                          BigDecimal longitude, BigDecimal locationAccuracy, LocalDateTime locationRecordedAt) {
        this(null, actualYield, actualYield, harvestDate, null,
                latitude, longitude, locationAccuracy, locationRecordedAt);
    }

    /** Cria nova instância vinculando ou substituindo o clientId (ex: extraído de Idempotency-Key). */
    public ProductionExecutionRegisterDTO withClientId(UUID resolvedClientId) {
        return new ProductionExecutionRegisterDTO(
                resolvedClientId,
                this.quantity,
                this.quantityKg,
                this.harvestedAt,
                this.notes,
                this.latitude,
                this.longitude,
                this.locationAccuracy,
                this.locationRecordedAt
        );
    }
}
