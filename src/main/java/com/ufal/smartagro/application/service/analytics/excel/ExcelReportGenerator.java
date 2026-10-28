package com.ufal.smartagro.application.service.analytics.excel;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerPlanSummaryCardDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCommunityPerformanceDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCropRankingItemDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementOrganizationSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianCommunitySummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianFarmerSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianPlanWarningDTO;
import com.ufal.smartagro.domain.model.enums.ManagementDashboardScope;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gerador de planilhas Excel (.xlsx) utilizando Apache POI.
 * Garante a preservação de tipos numéricos (§12 Modelagem v2.1)
 * com formatos dedicados para moeda/kg, área em ha, percentuais e inteiros.
 */
@Component
public class ExcelReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateFarmerSummaryXlsx(FarmerDashboardSummaryDTO data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ExcelStyles styles = new ExcelStyles(workbook);

            // 1. Aba de Resumo
            Sheet summarySheet = workbook.createSheet("Resumo Geral");
            buildFarmerSummarySheet(summarySheet, data, styles);

            // 2. Aba de Planos
            Sheet plansSheet = workbook.createSheet("Planos de Produção");
            buildFarmerPlansSheet(plansSheet, data.plans(), styles);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao gerar planilha XLSX do produtor", e);
        }
    }

    public byte[] generateTechnicianSummaryXlsx(TechnicianDashboardSummaryDTO data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ExcelStyles styles = new ExcelStyles(workbook);

            // 1. Aba Resumo
            Sheet summarySheet = workbook.createSheet("Resumo da Carteira");
            buildTechnicianSummarySheet(summarySheet, data, styles);

            // 2. Aba Comunidades
            Sheet communitiesSheet = workbook.createSheet("Comunidades");
            buildTechnicianCommunitiesSheet(communitiesSheet, data.communities(), styles);

            // 3. Aba Agricultores
            Sheet farmersSheet = workbook.createSheet("Agricultores");
            buildTechnicianFarmersSheet(farmersSheet, data.farmers(), styles);

            // 4. Aba Alertas
            if (data.warnings() != null && !data.warnings().isEmpty()) {
                Sheet warningsSheet = workbook.createSheet("Alertas Operacionais");
                buildTechnicianWarningsSheet(warningsSheet, data.warnings(), styles);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao gerar planilha XLSX do técnico", e);
        }
    }

    public byte[] generateManagementSummaryXlsx(ManagementDashboardSummaryDTO data) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ExcelStyles styles = new ExcelStyles(workbook);

            // 1. Aba Resumo
            Sheet summarySheet = workbook.createSheet("Visão Macro");
            buildManagementSummarySheet(summarySheet, data, styles);

            // 2. Aba Ranking Culturas
            Sheet rankingSheet = workbook.createSheet("Ranking de Culturas");
            buildManagementCropRankingSheet(rankingSheet, data.cropRanking(), styles);

            // 3. Aba Comunidades
            Sheet communitiesSheet = workbook.createSheet("Desempenho Comunidades");
            buildManagementCommunitiesSheet(communitiesSheet, data.communities(), data.scope(), styles);

            // 4. Aba Organizações (se escopo Global)
            if (data.scope() == ManagementDashboardScope.GLOBAL && data.organizations() != null && !data.organizations().isEmpty()) {
                Sheet orgsSheet = workbook.createSheet("Organizações");
                buildManagementOrganizationsSheet(orgsSheet, data.organizations(), styles);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao gerar planilha XLSX gerencial", e);
        }
    }

    // =========================================================================
    // Construção das Abas - Agricultor
    // =========================================================================

    private void buildFarmerSummarySheet(Sheet sheet, FarmerDashboardSummaryDTO data, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        int rowIdx = 0;

        rowIdx = createTitleRow(sheet, rowIdx, "SMARTAGRO - RESUMO CONSOLIDADO DO AGRICULTOR", styles);
        rowIdx++;

        rowIdx = createKeyValueRow(sheet, rowIdx, "Produtor:", data.farmerName(), styles);
        rowIdx = createKeyValueRow(sheet, rowIdx, "Comunidade:", data.communityName(), styles);
        rowIdx = createKeyValueRow(sheet, rowIdx, "Safra:", data.currentHarvest() != null ? data.currentHarvest().label() : "Safra Vigente", styles);
        rowIdx++;

        rowIdx = createSectionHeaderRow(sheet, rowIdx, "INDICADORES CONSOLIDADOS DA SAFRA", styles);

        rowIdx = createMetricRow(sheet, rowIdx, "Área Total Plantada (ha)", data.totalPlantedAreaHa(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Meta Total Prevista (kg)", data.totalTargetKg(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Colheita Total Validada (kg)", data.totalRealizedKg(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Colheita Pendente de Validação (kg)", data.totalPendingKg(), styles.number2Dec, styles);
        rowIdx = createMetricRowPct(sheet, rowIdx, "Conclusão Geral (%)", data.overallCompletionPct(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Total de Planos Cadastrados", data.totalPlansCount(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Talhões / Áreas Ativas", data.activePlotsCount(), styles);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildFarmerPlansSheet(Sheet sheet, List<FarmerPlanSummaryCardDTO> plans, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        sheet.createFreezePane(0, 1);

        String[] headers = {
                "Cultura", "Variedade", "Talhão / Localização", "Tipo Colheita", "Status", "Progresso",
                "Área (ha)", "Meta (kg)", "Realizado (kg)", "Pendente (kg)", "% Conclusão",
                "Data Plantio", "Início Previsto", "Fim Previsto", "Último Apontamento"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        int rowIdx = 1;
        if (plans != null) {
            for (FarmerPlanSummaryCardDTO p : plans) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, p.cropName(), styles.text);
                createCell(row, 1, p.cropVariety(), styles.text);
                createCell(row, 2, p.locationDescription(), styles.text);
                createCell(row, 3, p.harvestType() != null ? p.harvestType().name() : "-", styles.textCenter);
                createCell(row, 4, p.status() != null ? p.status().name() : "-", styles.textCenter);
                createCell(row, 5, p.progressStatus() != null ? p.progressStatus().name() : "-", styles.textCenter);

                createNumericCell(row, 6, p.plantedAreaHa(), styles.number2Dec);
                createNumericCell(row, 7, p.targetKg(), styles.number2Dec);
                createNumericCell(row, 8, p.realizedKg(), styles.number2Dec);
                createNumericCell(row, 9, p.pendingKg(), styles.number2Dec);
                createPctCell(row, 10, p.completionPct(), styles.percentage);

                createCell(row, 11, formatDate(p.plantingDate()), styles.textCenter);
                createCell(row, 12, formatDate(p.expectedHarvestStart()), styles.textCenter);
                createCell(row, 13, formatDate(p.expectedHarvestEnd()), styles.textCenter);
                createCell(row, 14, formatDate(p.lastExecutionDate()), styles.textCenter);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // =========================================================================
    // Construção das Abas - Técnico
    // =========================================================================

    private void buildTechnicianSummarySheet(Sheet sheet, TechnicianDashboardSummaryDTO data, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        int rowIdx = 0;

        rowIdx = createTitleRow(sheet, rowIdx, "SMARTAGRO - RESUMO DA CARTEIRA TÉCNICA", styles);
        rowIdx++;

        rowIdx = createKeyValueRow(sheet, rowIdx, "Técnico Responsável:", data.technicianName(), styles);
        rowIdx = createKeyValueRow(sheet, rowIdx, "Safra:", data.currentHarvest() != null ? data.currentHarvest().label() : "Safra Vigente", styles);
        rowIdx++;

        rowIdx = createSectionHeaderRow(sheet, rowIdx, "INDICADORES MACRO DA CARTEIRA", styles);

        rowIdx = createMetricRowInt(sheet, rowIdx, "Comunidades Assistidas", data.totalAssistedCommunitiesCount(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Produtores Assistidos", data.totalAssistedFarmersCount(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Total de Planos", data.totalPlansCount(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Planos Ativos", data.activePlansCount(), styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Área Total da Carteira (ha)", data.totalPlantedAreaHa(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Meta Total Prevista (kg)", data.totalTargetKg(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Colheita Total Validada (kg)", data.totalRealizedKg(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Colheita Pendente de Validação (kg)", data.totalPendingKg(), styles.number2Dec, styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Apontamentos Pendentes de Validação", data.pendingExecutionsCount(), styles);
        rowIdx = createMetricRowPct(sheet, rowIdx, "Conclusão Média da Carteira (%)", data.overallCompletionPct(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Total de Alertas Operacionais Ativos", data.totalWarningsCount(), styles);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildTechnicianCommunitiesSheet(Sheet sheet, List<TechnicianCommunitySummaryDTO> list, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        sheet.createFreezePane(0, 1);

        String[] headers = {
                "Comunidade", "Produtores Assistidos", "Produtores c/ Planos", "Total Planos",
                "Área Plantada (ha)", "Meta Prevista (kg)", "Realizado Validado (kg)",
                "Pendente (kg)", "% Conclusão", "Alertas Ativos"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        int rowIdx = 1;
        if (list != null) {
            for (TechnicianCommunitySummaryDTO c : list) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, c.communityName(), styles.text);
                createIntegerCell(row, 1, c.assistedFarmersCount(), styles.integer);
                createIntegerCell(row, 2, c.farmersWithPlansCount(), styles.integer);
                createIntegerCell(row, 3, c.plansCount(), styles.integer);
                createNumericCell(row, 4, c.plantedAreaHa(), styles.number2Dec);
                createNumericCell(row, 5, c.targetKg(), styles.number2Dec);
                createNumericCell(row, 6, c.realizedKg(), styles.number2Dec);
                createNumericCell(row, 7, c.pendingKg(), styles.number2Dec);
                createPctCell(row, 8, c.completionPct(), styles.percentage);
                createIntegerCell(row, 9, c.warningsCount(), styles.integer);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildTechnicianFarmersSheet(Sheet sheet, List<TechnicianFarmerSummaryDTO> list, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        sheet.createFreezePane(0, 1);

        String[] headers = {
                "Agricultor", "Comunidade", "Situação Cadastral", "Planos Ativos", "Total Planos",
                "Área (ha)", "Meta Prevista (kg)", "Realizado Validado (kg)", "Pendente (kg)",
                "% Conclusão", "Último Apontamento", "Alertas"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        int rowIdx = 1;
        if (list != null) {
            for (TechnicianFarmerSummaryDTO f : list) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, f.farmerName(), styles.text);
                createCell(row, 1, f.communityName(), styles.text);
                createCell(row, 2, f.isCompliant() ? "Adimplente" : "Inadimplente", styles.textCenter);
                createIntegerCell(row, 3, f.activePlansCount(), styles.integer);
                createIntegerCell(row, 4, f.plansCount(), styles.integer);
                createNumericCell(row, 5, f.plantedAreaHa(), styles.number2Dec);
                createNumericCell(row, 6, f.targetKg(), styles.number2Dec);
                createNumericCell(row, 7, f.realizedKg(), styles.number2Dec);
                createNumericCell(row, 8, f.pendingKg(), styles.number2Dec);
                createPctCell(row, 9, f.completionPct(), styles.percentage);
                createCell(row, 10, formatDate(f.lastExecutionDate()), styles.textCenter);
                createIntegerCell(row, 11, f.warningsCount(), styles.integer);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildTechnicianWarningsSheet(Sheet sheet, List<TechnicianPlanWarningDTO> list, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        sheet.createFreezePane(0, 1);

        String[] headers = {
                "Severidade", "Agricultor", "Comunidade", "Cultura", "Variedade",
                "Mensagem do Alerta", "Meta (kg)", "Realizado (kg)", "% Conclusão",
                "Dias sem Apontamento", "Fim Previsto"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        int rowIdx = 1;
        for (TechnicianPlanWarningDTO w : list) {
            Row row = sheet.createRow(rowIdx++);

            createCell(row, 0, w.severity() != null ? w.severity().name() : "-", styles.textCenter);
            createCell(row, 1, w.farmerName(), styles.text);
            createCell(row, 2, w.communityName(), styles.text);
            createCell(row, 3, w.cropName(), styles.text);
            createCell(row, 4, w.cropVariety(), styles.text);
            createCell(row, 5, w.message(), styles.text);
            createNumericCell(row, 6, w.targetKg(), styles.number2Dec);
            createNumericCell(row, 7, w.realizedKg(), styles.number2Dec);
            createPctCell(row, 8, w.completionPct(), styles.percentage);
            createIntegerCell(row, 9, w.daysWithoutExecution() != null ? w.daysWithoutExecution() : 0, styles.integer);
            createCell(row, 10, formatDate(w.expectedHarvestEnd()), styles.textCenter);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // =========================================================================
    // Construção das Abas - Gestor / Macro
    // =========================================================================

    private void buildManagementSummarySheet(Sheet sheet, ManagementDashboardSummaryDTO data, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        int rowIdx = 0;

        rowIdx = createTitleRow(sheet, rowIdx, "SMARTAGRO - RELATÓRIO MACRO-GERENCIAL DE PRODUÇÃO", styles);
        rowIdx++;

        String scopeLabel = data.scope() == ManagementDashboardScope.GLOBAL ? "Global (Administração Geral)" : "Organização Local";
        rowIdx = createKeyValueRow(sheet, rowIdx, "Escopo:", scopeLabel, styles);
        if (data.organizationName() != null) {
            rowIdx = createKeyValueRow(sheet, rowIdx, "Organização:", data.organizationName(), styles);
        }
        rowIdx = createKeyValueRow(sheet, rowIdx, "Safra:", data.currentHarvest() != null ? data.currentHarvest().label() : "Safra Vigente", styles);
        rowIdx++;

        rowIdx = createSectionHeaderRow(sheet, rowIdx, "INDICADORES MACRO-GERENCIAIS", styles);

        rowIdx = createMetricRowInt(sheet, rowIdx, "Total de Organizações", data.totalOrganizationsCount(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Total de Comunidades Atendidas", data.totalCommunitiesCount(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Produtores Ativos", data.totalFarmersCount(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Produtores Adimplentes", data.compliantFarmersCount(), styles);
        rowIdx = createMetricRowPct(sheet, rowIdx, "Taxa de Adimplência (%)", data.complianceRatePct(), styles);
        rowIdx = createMetricRowInt(sheet, rowIdx, "Total de Planos de Produção", data.totalPlansCount(), styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Área Total Plantada (ha)", data.totalPlantedAreaHa(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Meta Total Prevista (kg)", data.totalTargetKg(), styles.number2Dec, styles);
        rowIdx = createMetricRow(sheet, rowIdx, "Produção Total Realizada (kg)", data.totalRealizedKg(), styles.number2Dec, styles);
        rowIdx = createMetricRowPct(sheet, rowIdx, "Taxa Global de Conversão (%)", data.overallConversionRatePct(), styles);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildManagementCropRankingSheet(Sheet sheet, List<ManagementCropRankingItemDTO> ranking, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        sheet.createFreezePane(0, 1);

        String[] headers = {
                "Posição", "Cultura", "Variedade", "Unidade", "Planos",
                "Área Total (ha)", "Meta Prevista (kg)", "Realizado Validado (kg)",
                "Taxa Conversão (%)", "Participação / Share (%)"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        int rowIdx = 1;
        if (ranking != null) {
            for (ManagementCropRankingItemDTO c : ranking) {
                Row row = sheet.createRow(rowIdx++);

                createIntegerCell(row, 0, c.rankPosition(), styles.integer);
                createCell(row, 1, c.cropName(), styles.text);
                createCell(row, 2, c.cropVariety(), styles.text);
                createCell(row, 3, c.unit(), styles.textCenter);
                createIntegerCell(row, 4, c.plansCount(), styles.integer);
                createNumericCell(row, 5, c.plantedAreaHa(), styles.number2Dec);
                createNumericCell(row, 6, c.targetKg(), styles.number2Dec);
                createNumericCell(row, 7, c.realizedKg(), styles.number2Dec);
                createPctCell(row, 8, c.conversionRatePct(), styles.percentage);
                createPctCell(row, 9, c.productionSharePct(), styles.percentage);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildManagementCommunitiesSheet(Sheet sheet, List<ManagementCommunityPerformanceDTO> list,
                                                ManagementDashboardScope scope, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        sheet.createFreezePane(0, 1);

        boolean showOrg = scope == ManagementDashboardScope.GLOBAL;
        String[] headers = showOrg ? new String[]{
                "Comunidade", "Organização", "Produtores Ativos", "Adimplentes",
                "% Adimplência", "Total Planos", "Área (ha)", "Meta (kg)",
                "Realizado (kg)", "Taxa Conversão (%)", "Último Apontamento"
        } : new String[]{
                "Comunidade", "Produtores Ativos", "Adimplentes",
                "% Adimplência", "Total Planos", "Área (ha)", "Meta (kg)",
                "Realizado (kg)", "Taxa Conversão (%)", "Último Apontamento"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        int rowIdx = 1;
        if (list != null) {
            for (ManagementCommunityPerformanceDTO c : list) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                createCell(row, col++, c.communityName(), styles.text);
                if (showOrg) {
                    createCell(row, col++, c.organizationName(), styles.text);
                }
                createIntegerCell(row, col++, c.activeFarmersCount(), styles.integer);
                createIntegerCell(row, col++, c.compliantFarmersCount(), styles.integer);
                createPctCell(row, col++, c.complianceRatePct(), styles.percentage);
                createIntegerCell(row, col++, c.plansCount(), styles.integer);
                createNumericCell(row, col++, c.plantedAreaHa(), styles.number2Dec);
                createNumericCell(row, col++, c.targetKg(), styles.number2Dec);
                createNumericCell(row, col++, c.realizedKg(), styles.number2Dec);
                createPctCell(row, col++, c.conversionRatePct(), styles.percentage);
                createCell(row, col++, formatDate(c.lastExecutionDate()), styles.textCenter);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildManagementOrganizationsSheet(Sheet sheet, List<ManagementOrganizationSummaryDTO> list, ExcelStyles styles) {
        sheet.setDisplayGridlines(true);
        sheet.createFreezePane(0, 1);

        String[] headers = {
                "Organização", "Comunidades", "Produtores Ativos", "Produtores Adimplentes",
                "% Adimplência", "Total Planos", "Área (ha)", "Meta Prevista (kg)",
                "Realizado Validado (kg)", "Taxa Conversão (%)"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.tableHeader);
        }

        int rowIdx = 1;
        if (list != null) {
            for (ManagementOrganizationSummaryDTO o : list) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, o.organizationName(), styles.text);
                createIntegerCell(row, 1, o.communitiesCount(), styles.integer);
                createIntegerCell(row, 2, o.activeFarmersCount(), styles.integer);
                createIntegerCell(row, 3, o.compliantFarmersCount(), styles.integer);
                createPctCell(row, 4, o.complianceRatePct(), styles.percentage);
                createIntegerCell(row, 5, o.plansCount(), styles.integer);
                createNumericCell(row, 6, o.plantedAreaHa(), styles.number2Dec);
                createNumericCell(row, 7, o.targetKg(), styles.number2Dec);
                createNumericCell(row, 8, o.realizedKg(), styles.number2Dec);
                createPctCell(row, 9, o.conversionRatePct(), styles.percentage);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // =========================================================================
    // Helpers de Células e Formatação
    // =========================================================================

    private int createTitleRow(Sheet sheet, int rowIdx, String title, ExcelStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(styles.docTitle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 5));
        return rowIdx + 1;
    }

    private int createSectionHeaderRow(Sheet sheet, int rowIdx, String title, ExcelStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(styles.sectionTitle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));
        return rowIdx + 1;
    }

    private int createKeyValueRow(Sheet sheet, int rowIdx, String key, String value, ExcelStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell kCell = row.createCell(0);
        kCell.setCellValue(key);
        kCell.setCellStyle(styles.label);

        Cell vCell = row.createCell(1);
        vCell.setCellValue(value != null ? value : "-");
        vCell.setCellStyle(styles.text);
        return rowIdx + 1;
    }

    private int createMetricRow(Sheet sheet, int rowIdx, String label, BigDecimal value, CellStyle numStyle, ExcelStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell lCell = row.createCell(0);
        lCell.setCellValue(label);
        lCell.setCellStyle(styles.label);

        Cell vCell = row.createCell(1);
        vCell.setCellValue(value != null ? value.doubleValue() : 0.0);
        vCell.setCellStyle(numStyle);
        return rowIdx + 1;
    }

    private int createMetricRowPct(Sheet sheet, int rowIdx, String label, BigDecimal pctValue, ExcelStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell lCell = row.createCell(0);
        lCell.setCellValue(label);
        lCell.setCellStyle(styles.label);

        Cell vCell = row.createCell(1);
        double val = pctValue != null ? pctValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP).doubleValue() : 0.0;
        vCell.setCellValue(val);
        vCell.setCellStyle(styles.percentage);
        return rowIdx + 1;
    }

    private int createMetricRowInt(Sheet sheet, int rowIdx, String label, int value, ExcelStyles styles) {
        Row row = sheet.createRow(rowIdx);
        Cell lCell = row.createCell(0);
        lCell.setCellValue(label);
        lCell.setCellStyle(styles.label);

        Cell vCell = row.createCell(1);
        vCell.setCellValue(value);
        vCell.setCellStyle(styles.integer);
        return rowIdx + 1;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "-");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value.doubleValue() : 0.0);
        cell.setCellStyle(style);
    }

    private void createIntegerCell(Row row, int col, int value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createPctCell(Row row, int col, BigDecimal pctValue, CellStyle style) {
        Cell cell = row.createCell(col);
        double val = pctValue != null ? pctValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP).doubleValue() : 0.0;
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    /**
     * Cache de estilos de células reutilizáveis do Apache POI.
     */
    private static class ExcelStyles {
        final CellStyle docTitle;
        final CellStyle sectionTitle;
        final CellStyle label;
        final CellStyle tableHeader;
        final CellStyle text;
        final CellStyle textCenter;
        final CellStyle number2Dec;
        final CellStyle integer;
        final CellStyle percentage;

        ExcelStyles(XSSFWorkbook wb) {
            DataFormat df = wb.createDataFormat();

            // Fontes
            Font fTitle = wb.createFont();
            fTitle.setBold(true);
            fTitle.setFontHeightInPoints((short) 14);
            fTitle.setColor(IndexedColors.DARK_GREEN.getIndex());

            Font fSection = wb.createFont();
            fSection.setBold(true);
            fSection.setFontHeightInPoints((short) 11);
            fSection.setColor(IndexedColors.DARK_GREEN.getIndex());

            Font fHeader = wb.createFont();
            fHeader.setBold(true);
            fHeader.setFontHeightInPoints((short) 9);
            fHeader.setColor(IndexedColors.WHITE.getIndex());

            Font fLabel = wb.createFont();
            fLabel.setBold(true);
            fLabel.setFontHeightInPoints((short) 9);

            Font fRegular = wb.createFont();
            fRegular.setFontHeightInPoints((short) 9);

            // Estilos
            docTitle = wb.createCellStyle();
            docTitle.setFont(fTitle);

            sectionTitle = wb.createCellStyle();
            sectionTitle.setFont(fSection);

            label = wb.createCellStyle();
            label.setFont(fLabel);
            applyBorders(label);

            tableHeader = wb.createCellStyle();
            tableHeader.setFont(fHeader);
            tableHeader.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            tableHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            tableHeader.setAlignment(HorizontalAlignment.CENTER);
            tableHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            applyBorders(tableHeader);

            text = wb.createCellStyle();
            text.setFont(fRegular);
            applyBorders(text);

            textCenter = wb.createCellStyle();
            textCenter.setFont(fRegular);
            textCenter.setAlignment(HorizontalAlignment.CENTER);
            applyBorders(textCenter);

            number2Dec = wb.createCellStyle();
            number2Dec.setFont(fRegular);
            number2Dec.setDataFormat(df.getFormat("#,##0.00"));
            number2Dec.setAlignment(HorizontalAlignment.RIGHT);
            applyBorders(number2Dec);

            integer = wb.createCellStyle();
            integer.setFont(fRegular);
            integer.setDataFormat(df.getFormat("#,##0"));
            integer.setAlignment(HorizontalAlignment.RIGHT);
            applyBorders(integer);

            percentage = wb.createCellStyle();
            percentage.setFont(fRegular);
            percentage.setDataFormat(df.getFormat("0.00%"));
            percentage.setAlignment(HorizontalAlignment.RIGHT);
            applyBorders(percentage);
        }

        private void applyBorders(CellStyle style) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }
}
