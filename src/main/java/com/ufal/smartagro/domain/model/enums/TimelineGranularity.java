package com.ufal.smartagro.domain.model.enums;

public enum TimelineGranularity {
    WEEK("Semana"),
    MONTH("Mês");

    private final String description;

    TimelineGranularity(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static TimelineGranularity fromString(String value) {
        if (value == null || value.isBlank()) {
            return WEEK;
        }

        String normalized = value.trim();

        if ("week".equalsIgnoreCase(normalized) || "semana".equalsIgnoreCase(normalized)) {
            return WEEK;
        }

        if ("month".equalsIgnoreCase(normalized) || "mes".equalsIgnoreCase(normalized) || "mês".equalsIgnoreCase(normalized)) {
            return MONTH;
        }

        throw new IllegalArgumentException("Granularidade inválida: '" + value + "'. Valores permitidos: WEEK, MONTH.");
    }
}
