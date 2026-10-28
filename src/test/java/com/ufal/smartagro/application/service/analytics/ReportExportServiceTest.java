package com.ufal.smartagro.application.service.analytics;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerPlanSummaryCardDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestSummaryInfoDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCommunityPerformanceDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCropRankingItemDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementOrganizationSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianCommunitySummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianFarmerSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianPlanWarningDTO;
import com.ufal.smartagro.application.service.analytics.excel.ExcelReportGenerator;
import com.ufal.smartagro.application.service.analytics.pdf.PdfReportGenerator;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.ManagementDashboardScope;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.TechnicianWarningType;
import com.ufal.smartagro.domain.model.enums.WarningSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportExportServiceTest {

    private ReportExportService reportExportService;

    @BeforeEach
    void setUp() {
        PdfReportGenerator pdfGenerator = new PdfReportGenerator();
        ExcelReportGenerator excelGenerator = new ExcelReportGenerator();
        reportExportService = new ReportExportService(pdfGenerator, excelGenerator);
    }

    @Test
    @DisplayName("Deve gerar PDF válido para resumo do agricultor")
    void shouldGenerateValidPdfForFarmerSummary() {
        FarmerDashboardSummaryDTO summary = createSampleFarmerSummary();

        byte[] pdfBytes = reportExportService.exportFarmerSummaryToPdf(summary);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(isPdf(pdfBytes)).isTrue();
    }

    @Test
    @DisplayName("Deve gerar XLSX válido para resumo do agricultor")
    void shouldGenerateValidXlsxForFarmerSummary() {
        FarmerDashboardSummaryDTO summary = createSampleFarmerSummary();

        byte[] xlsxBytes = reportExportService.exportFarmerSummaryToXlsx(summary);

        assertThat(xlsxBytes).isNotEmpty();
        assertThat(isZipOrXlsx(xlsxBytes)).isTrue();
    }

    @Test
    @DisplayName("Deve gerar PDF e XLSX para resumo vazio do agricultor")
    void shouldGenerateReportsForEmptyFarmerSummary() {
        FarmerDashboardSummaryDTO emptySummary = FarmerDashboardSummaryDTO.empty(
                UUID.randomUUID(), "João da Silva", "Comunidade Boa Esperança",
                new HarvestSummaryInfoDTO(UUID.randomUUID(), "Safra 2026/1", LocalDate.now(), LocalDate.now().plusMonths(6))
        );

        byte[] pdfBytes = reportExportService.exportFarmerSummaryToPdf(emptySummary);
        byte[] xlsxBytes = reportExportService.exportFarmerSummaryToXlsx(emptySummary);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(isPdf(pdfBytes)).isTrue();
        assertThat(xlsxBytes).isNotEmpty();
        assertThat(isZipOrXlsx(xlsxBytes)).isTrue();
    }

    @Test
    @DisplayName("Deve gerar PDF e XLSX válidos para resumo do técnico com alertas")
    void shouldGenerateValidReportsForTechnicianSummary() {
        TechnicianDashboardSummaryDTO summary = createSampleTechnicianSummary();

        byte[] pdfBytes = reportExportService.exportTechnicianSummaryToPdf(summary);
        byte[] xlsxBytes = reportExportService.exportTechnicianSummaryToXlsx(summary);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(isPdf(pdfBytes)).isTrue();
        assertThat(xlsxBytes).isNotEmpty();
        assertThat(isZipOrXlsx(xlsxBytes)).isTrue();
    }

    @Test
    @DisplayName("Deve gerar PDF e XLSX válidos para resumo gerencial em escopo Global")
    void shouldGenerateValidReportsForManagementGlobalSummary() {
        ManagementDashboardSummaryDTO summary = createSampleManagementSummary(ManagementDashboardScope.GLOBAL);

        byte[] pdfBytes = reportExportService.exportManagementSummaryToPdf(summary);
        byte[] xlsxBytes = reportExportService.exportManagementSummaryToXlsx(summary);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(isPdf(pdfBytes)).isTrue();
        assertThat(xlsxBytes).isNotEmpty();
        assertThat(isZipOrXlsx(xlsxBytes)).isTrue();
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para entradas nulas")
    void shouldThrowExceptionWhenDataIsNull() {
        assertThatThrownBy(() -> reportExportService.exportFarmerSummaryToPdf(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportExportService.exportFarmerSummaryToXlsx(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportExportService.exportTechnicianSummaryToPdf(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportExportService.exportTechnicianSummaryToXlsx(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportExportService.exportManagementSummaryToPdf(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> reportExportService.exportManagementSummaryToXlsx(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==========================================
    // Helpers de validação de Magic Bytes
    // ==========================================

    private boolean isPdf(byte[] data) {
        // PDF Magic Number: "%PDF" (0x25, 0x50, 0x44, 0x46)
        return data.length >= 4 &&
                data[0] == 0x25 &&
                data[1] == 0x50 &&
                data[2] == 0x44 &&
                data[3] == 0x46;
    }

    private boolean isZipOrXlsx(byte[] data) {
        // ZIP/XLSX Magic Number: "PK" (0x50, 0x4B, 0x03, 0x04)
        return data.length >= 4 &&
                data[0] == 0x50 &&
                data[1] == 0x4B &&
                data[2] == 0x03 &&
                data[3] == 0x04;
    }

    // ==========================================
    // Helpers de criação de Mocks
    // ==========================================

    private FarmerDashboardSummaryDTO createSampleFarmerSummary() {
        FarmerPlanSummaryCardDTO plan = new FarmerPlanSummaryCardDTO(
                UUID.randomUUID(), UUID.randomUUID(), "Milho", "BRS 1055",
                HarvestType.CONTINUOUS, "kg", PlanStatus.ACTIVE, "Talhão 01 - Norte",
                BigDecimal.valueOf(2.5), BigDecimal.valueOf(5000.0), BigDecimal.valueOf(3200.0),
                BigDecimal.valueOf(1800.0), BigDecimal.valueOf(64.0), PlanProgressStatus.ON_TRACK,
                LocalDate.now().minusMonths(3), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(2),
                LocalDate.now().minusDays(2)
        );

        return new FarmerDashboardSummaryDTO(
                UUID.randomUUID(), "José de Alencar", "Comunidade São José",
                new HarvestSummaryInfoDTO(UUID.randomUUID(), "Safra Principal 2026", LocalDate.now().minusMonths(4), LocalDate.now().plusMonths(4)),
                false, 1, 1, BigDecimal.valueOf(2.5), BigDecimal.valueOf(5000.0),
                BigDecimal.valueOf(3200.0), BigDecimal.valueOf(1800.0), BigDecimal.valueOf(64.0),
                List.of(plan)
        );
    }

    private TechnicianDashboardSummaryDTO createSampleTechnicianSummary() {
        TechnicianCommunitySummaryDTO comm = new TechnicianCommunitySummaryDTO(
                UUID.randomUUID(), "Comunidade Lagoa Seca", 10, 8, 12,
                BigDecimal.valueOf(15.0), BigDecimal.valueOf(30000.0), BigDecimal.valueOf(18000.0),
                BigDecimal.valueOf(12000.0), BigDecimal.valueOf(60.0), 1
        );

        TechnicianFarmerSummaryDTO farmer = new TechnicianFarmerSummaryDTO(
                UUID.randomUUID(), "Maria Pereira", UUID.randomUUID(), "Comunidade Lagoa Seca",
                true, 2, 2, BigDecimal.valueOf(3.0), BigDecimal.valueOf(6000.0),
                BigDecimal.valueOf(4500.0), BigDecimal.valueOf(1500.0), BigDecimal.valueOf(75.0),
                LocalDate.now().minusDays(1), 0
        );

        TechnicianPlanWarningDTO warning = new TechnicianPlanWarningDTO(
                UUID.randomUUID(), UUID.randomUUID(), "Antônio Carlos", UUID.randomUUID(),
                "Comunidade Lagoa Seca", UUID.randomUUID(), "Mandioca", "Vencedora",
                PlanStatus.ACTIVE, PlanProgressStatus.BELOW_EXPECTED,
                List.of(TechnicianWarningType.LOW_YIELD_RISK), WarningSeverity.HIGH,
                "Colheita abaixo do ritmo previsto", BigDecimal.valueOf(4000.0), BigDecimal.valueOf(1000.0),
                BigDecimal.valueOf(3000.0), BigDecimal.valueOf(2000.0), BigDecimal.valueOf(25.0),
                LocalDate.now().minusDays(15), 15, LocalDate.now().minusDays(20), LocalDate.now().plusDays(20)
        );

        return new TechnicianDashboardSummaryDTO(
                UUID.randomUUID(), "Técnico Agrícola João",
                new HarvestSummaryInfoDTO(UUID.randomUUID(), "Safra 2026", LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(6)),
                null, null, false, 1, 1, 2, 2,
                BigDecimal.valueOf(15.0), BigDecimal.valueOf(30000.0), BigDecimal.valueOf(18000.0),
                BigDecimal.valueOf(12000.0), 0, BigDecimal.valueOf(60.0), 1,
                List.of(warning), List.of(comm), List.of(farmer)
        );
    }

    private ManagementDashboardSummaryDTO createSampleManagementSummary(ManagementDashboardScope scope) {
        ManagementCropRankingItemDTO crop = new ManagementCropRankingItemDTO(
                1, UUID.randomUUID(), "Milho", "Híbrido", "kg", 15,
                BigDecimal.valueOf(45.0), BigDecimal.valueOf(90000.0), BigDecimal.valueOf(72000.0),
                BigDecimal.valueOf(80.0), BigDecimal.valueOf(60.0)
        );

        ManagementCommunityPerformanceDTO comm = new ManagementCommunityPerformanceDTO(
                UUID.randomUUID(), "Comunidade Santa Fé", UUID.randomUUID(), "Cooperativa Central",
                12, 10, BigDecimal.valueOf(83.33), 15, BigDecimal.valueOf(45.0),
                BigDecimal.valueOf(90000.0), BigDecimal.valueOf(72000.0), BigDecimal.valueOf(80.0),
                LocalDate.now().minusDays(1)
        );

        ManagementOrganizationSummaryDTO org = new ManagementOrganizationSummaryDTO(
                UUID.randomUUID(), "Cooperativa Central", 5, 50, 45,
                BigDecimal.valueOf(90.0), 60, BigDecimal.valueOf(180.0),
                BigDecimal.valueOf(350000.0), BigDecimal.valueOf(290000.0), BigDecimal.valueOf(82.85)
        );

        return new ManagementDashboardSummaryDTO(
                scope, UUID.randomUUID(), "Cooperativa Central",
                new HarvestSummaryInfoDTO(UUID.randomUUID(), "Safra Geral 2026", LocalDate.now().minusMonths(3), LocalDate.now().plusMonths(5)),
                null, false, 1, 1, 50, 45, BigDecimal.valueOf(90.0), 60,
                BigDecimal.valueOf(180.0), BigDecimal.valueOf(350000.0), BigDecimal.valueOf(290000.0),
                BigDecimal.valueOf(82.85), List.of(crop), List.of(comm), List.of(org)
        );
    }
}
