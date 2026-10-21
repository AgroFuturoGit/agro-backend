package com.ufal.smartagro.adapters.in.web.dto.production;

import jakarta.validation.constraints.NotBlank;

/** Justificativa para a rejeição de um apontamento de colheita. */
public record RejectExecutionDTO(
        @NotBlank(message = "A justificativa da rejeição é obrigatória.") String reason
) {}
