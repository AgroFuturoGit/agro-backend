package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Campos editáveis do apontamento, incluindo quantidade, horário e decisão de validação. */
public record ProductionExecutionUpdateDTO(
        @DecimalMin(value = "0.01", message = "A quantidade produzida deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "A quantidade deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal quantity,

        @DecimalMin(value = "0.01", message = "A quantidade em kg deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "A quantidade em kg deve ter no máximo 8 dígitos inteiros e 2 decimais.")
        BigDecimal quantityKg,

        @PastOrPresent(message = "A data da colheita não pode estar no futuro.")
        LocalDate harvestedAt,

        com.ufal.smartagro.domain.model.enums.ExecutionStatus status,
        UUID validatedBy,
        LocalDateTime validatedAt,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.")
        String notes,

        @DecimalMin(value = "-90.0", message = "Latitude inválida.")
        @DecimalMax(value = "90.0", message = "Latitude inválida.")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude inválida.")
        @DecimalMax(value = "180.0", message = "Longitude inválida.")
        BigDecimal longitude,

        @DecimalMin(value = "0.0", message = "A precisão não pode ser negativa.")
        BigDecimal locationAccuracy,

        LocalDateTime locationRecordedAt,

        /**
         * Remove a localização do apontamento. Necessário porque um campo nulo
         * significa "não mexer", então sem este sinal não haveria como apagar
         * uma posição já gravada.
         */
        Boolean clearLocation,

        /**
         * Versão do apontamento que o cliente tinha em mãos ao editar. Quando
         * enviada, o servidor recusa a escrita com 409 caso já tenha avançado
         * além dela. Omitir mantém o comportamento de sobrescrita direta.
         */
        LocalDateTime baseUpdatedAt
) {
    /** Construtor de compatibilidade; converte o rendimento em quantidade e peso em kg. */
    public ProductionExecutionUpdateDTO(BigDecimal actualYield, LocalDate harvestDate, BigDecimal latitude,
                                        BigDecimal longitude, BigDecimal locationAccuracy, LocalDateTime locationRecordedAt,
                                        Boolean clearLocation, LocalDateTime baseUpdatedAt) {
        this(actualYield, actualYield, harvestDate, null, null, null, null,
                latitude, longitude, locationAccuracy, locationRecordedAt, clearLocation, baseUpdatedAt);
    }
}
