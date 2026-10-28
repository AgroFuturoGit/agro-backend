package com.ufal.smartagro.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportExportFormatTest {

    @Test
    @DisplayName("Deve retornar PDF por padrão quando o valor for nulo ou vazio")
    void shouldReturnDefaultPdfWhenNullOrBlank() {
        assertThat(ReportExportFormat.fromString(null)).isEqualTo(ReportExportFormat.PDF);
        assertThat(ReportExportFormat.fromString("")).isEqualTo(ReportExportFormat.PDF);
        assertThat(ReportExportFormat.fromString("   ")).isEqualTo(ReportExportFormat.PDF);
    }

    @ParameterizedTest
    @ValueSource(strings = {"pdf", "PDF", "Pdf", "  pdf  "})
    @DisplayName("Deve reconhecer variações de PDF")
    void shouldRecognizePdf(String input) {
        assertThat(ReportExportFormat.fromString(input)).isEqualTo(ReportExportFormat.PDF);
        assertThat(ReportExportFormat.PDF.getExtension()).isEqualTo("pdf");
        assertThat(ReportExportFormat.PDF.getMediaType()).isEqualTo("application/pdf");
    }

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "XLSX", "excel", "EXCEL", "  xlsx  "})
    @DisplayName("Deve reconhecer variações de XLSX e EXCEL")
    void shouldRecognizeXlsx(String input) {
        assertThat(ReportExportFormat.fromString(input)).isEqualTo(ReportExportFormat.XLSX);
        assertThat(ReportExportFormat.XLSX.getExtension()).isEqualTo("xlsx");
        assertThat(ReportExportFormat.XLSX.getMediaType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @ParameterizedTest
    @ValueSource(strings = {"csv", "doc", "html", "txt", "zip"})
    @DisplayName("Deve lançar IllegalArgumentException para formatos não suportados")
    void shouldThrowExceptionForUnsupportedFormats(String input) {
        assertThatThrownBy(() -> ReportExportFormat.fromString(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato de exportação inválido");
    }
}
