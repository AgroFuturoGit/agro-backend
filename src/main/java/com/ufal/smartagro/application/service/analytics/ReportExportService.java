package com.ufal.smartagro.application.service.analytics;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.application.service.analytics.excel.ExcelReportGenerator;
import com.ufal.smartagro.application.service.analytics.pdf.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Serviço de exportação de relatórios analíticos em PDF e planilhas Excel (.xlsx).
 * Atende à Feature 6.5 (Motor de Exportação de Relatórios de Produção).
 */
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final PdfReportGenerator pdfReportGenerator;
    private final ExcelReportGenerator excelReportGenerator;

    /**
     * Exporta o resumo analítico individual do agricultor para formato PDF.
     */
    public byte[] exportFarmerSummaryToPdf(FarmerDashboardSummaryDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Dados do agricultor não podem ser nulos para exportação.");
        }
        return pdfReportGenerator.generateFarmerSummaryPdf(data);
    }

    /**
     * Exporta o resumo analítico individual do agricultor para planilha XLSX.
     */
    public byte[] exportFarmerSummaryToXlsx(FarmerDashboardSummaryDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Dados do agricultor não podem ser nulos para exportação.");
        }
        return excelReportGenerator.generateFarmerSummaryXlsx(data);
    }

    /**
     * Exporta o resumo analítico da carteira técnica para formato PDF.
     */
    public byte[] exportTechnicianSummaryToPdf(TechnicianDashboardSummaryDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Dados da carteira técnica não podem ser nulos para exportação.");
        }
        return pdfReportGenerator.generateTechnicianSummaryPdf(data);
    }

    /**
     * Exporta o resumo analítico da carteira técnica para planilha XLSX.
     */
    public byte[] exportTechnicianSummaryToXlsx(TechnicianDashboardSummaryDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Dados da carteira técnica não podem ser nulos para exportação.");
        }
        return excelReportGenerator.generateTechnicianSummaryXlsx(data);
    }

    /**
     * Exporta o resumo macro-gerencial (Organização ou Global) para formato PDF.
     */
    public byte[] exportManagementSummaryToPdf(ManagementDashboardSummaryDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Dados gerenciais não podem ser nulos para exportação.");
        }
        return pdfReportGenerator.generateManagementSummaryPdf(data);
    }

    /**
     * Exporta o resumo macro-gerencial (Organização ou Global) para planilha XLSX.
     */
    public byte[] exportManagementSummaryToXlsx(ManagementDashboardSummaryDTO data) {
        if (data == null) {
            throw new IllegalArgumentException("Dados gerenciais não podem ser nulos para exportação.");
        }
        return excelReportGenerator.generateManagementSummaryXlsx(data);
    }
}
