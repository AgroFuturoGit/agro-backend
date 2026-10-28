package com.ufal.smartagro.domain.model.enums;

/**
 * Formatos de exportação suportados pelo motor de relatórios (Feature 6.5).
 */
public enum ReportExportFormat {
    PDF("pdf", "application/pdf"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String extension;
    private final String mediaType;

    ReportExportFormat(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static ReportExportFormat fromString(String value) {
        if (value == null || value.isBlank()) {
            return PDF;
        }

        String normalized = value.trim().toLowerCase();
        if ("pdf".equals(normalized)) {
            return PDF;
        }
        if ("xlsx".equals(normalized) || "excel".equals(normalized)) {
            return XLSX;
        }

        throw new IllegalArgumentException("Formato de exportação inválido: '" + value + "'. Formatos suportados: pdf, xlsx.");
    }
}
