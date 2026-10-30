package com.ufal.smartagro.application.service.analytics.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
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
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Gerador de relatórios analíticos em PDF utilizando OpenPDF.
 */
@Component
public class PdfReportGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");

    private static final Color COLOR_PRIMARY = new Color(34, 112, 62);
    private static final Color COLOR_PRIMARY_LIGHT = new Color(236, 245, 238);
    private static final Color COLOR_HEADER_BG = new Color(46, 125, 50);
    private static final Color COLOR_ZEBRA_BG = new Color(248, 250, 248);
    private static final Color COLOR_BORDER = new Color(220, 226, 222);
    private static final Color COLOR_TEXT_MUTED = new Color(110, 120, 115);
    private static final Color COLOR_WARNING = new Color(194, 65, 12);
    private static final Color COLOR_CRITICAL = new Color(185, 28, 28);

    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_HEADER_BG);
    private static final Font FONT_META = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_TEXT_MUTED);
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_TABLE_HEADER = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font FONT_TABLE_CELL = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FONT_TABLE_CELL_BOLD = new Font(Font.HELVETICA, 8, Font.BOLD, Color.DARK_GRAY);
    private static final Font FONT_KPI_LABEL = new Font(Font.HELVETICA, 7, Font.NORMAL, COLOR_TEXT_MUTED);
    private static final Font FONT_KPI_VALUE = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 7, Font.NORMAL, COLOR_TEXT_MUTED);

    public byte[] generateFarmerSummaryPdf(FarmerDashboardSummaryDTO data) {
        return buildPdf(document -> {
            addDocumentHeader(document, "RELATÓRIO CONSOLIDADO DO AGRICULTOR",
                    "Produtor: " + (data.farmerName() != null ? data.farmerName() : "-"),
                    "Comunidade: " + (data.communityName() != null ? data.communityName() : "-"),
                    getHarvestLabel(data.currentHarvest() != null ? data.currentHarvest().label() : null));

            // KPI Cards
            addKpiRow(document, List.of(
                    new Kpi("Área Plantada", formatHa(data.totalPlantedAreaHa())),
                    new Kpi("Meta Prevista", formatKg(data.totalTargetKg())),
                    new Kpi("Colheita Validada", formatKg(data.totalRealizedKg())),
                    new Kpi("Pendente de Validação", formatKg(data.totalPendingKg())),
                    new Kpi("Conclusão Geral", formatPct(data.overallCompletionPct())),
                    new Kpi("Total de Planos", String.valueOf(data.totalPlansCount()))
            ));

            addSpacer(document, 8);
            addSectionTitle(document, "Planos de Produção e Talhões");

            if (data.isEmpty() || data.plans() == null || data.plans().isEmpty()) {
                addEmptyStateNotice(document, "Nenhum plano de produção cadastrado para a safra informada.");
            } else {
                addFarmerPlansTable(document, data.plans());
            }
        });
    }

    public byte[] generateTechnicianSummaryPdf(TechnicianDashboardSummaryDTO data) {
        return buildPdf(document -> {
            addDocumentHeader(document, "RELATÓRIO OPERACIONAL DA CARTEIRA TÉCNICA",
                    "Técnico Responsável: " + (data.technicianName() != null ? data.technicianName() : "-"),
                    "Filtro Comunidade: " + (data.appliedCommunityId() != null ? data.appliedCommunityId().toString() : "Todas"),
                    getHarvestLabel(data.currentHarvest() != null ? data.currentHarvest().label() : null));

            // KPI Cards
            addKpiRow(document, List.of(
                    new Kpi("Comunidades Assistidas", String.valueOf(data.totalAssistedCommunitiesCount())),
                    new Kpi("Produtores Assistidos", String.valueOf(data.totalAssistedFarmersCount())),
                    new Kpi("Planos Ativos / Total", data.activePlansCount() + " / " + data.totalPlansCount()),
                    new Kpi("Área Total (ha)", formatHa(data.totalPlantedAreaHa())),
                    new Kpi("Meta Total", formatKg(data.totalTargetKg())),
                    new Kpi("Realizado Total", formatKg(data.totalRealizedKg())),
                    new Kpi("Progresso da Carteira", formatPct(data.overallCompletionPct())),
                    new Kpi("Alertas Ativos", String.valueOf(data.totalWarningsCount()))
            ));

            addSpacer(document, 8);
            addSectionTitle(document, "Resumo por Comunidade Assistida");
            if (data.communities() == null || data.communities().isEmpty()) {
                addEmptyStateNotice(document, "Nenhuma comunidade assistida registrada.");
            } else {
                addTechnicianCommunitiesTable(document, data.communities());
            }

            addSpacer(document, 8);
            addSectionTitle(document, "Desempenho dos Agricultores Assistidos");
            if (data.farmers() == null || data.farmers().isEmpty()) {
                addEmptyStateNotice(document, "Nenhum agricultor vinculado encontrado.");
            } else {
                addTechnicianFarmersTable(document, data.farmers());
            }

            if (data.warnings() != null && !data.warnings().isEmpty()) {
                addSpacer(document, 8);
                addSectionTitle(document, "Alertas Operacionais Pendentes (" + data.warnings().size() + ")");
                addTechnicianWarningsTable(document, data.warnings());
            }
        });
    }

    public byte[] generateManagementSummaryPdf(ManagementDashboardSummaryDTO data) {
        return buildPdf(document -> {
            String scopeLabel = data.scope() == ManagementDashboardScope.GLOBAL
                    ? "Global (Administração Geral)"
                    : "Organização: " + (data.organizationName() != null ? data.organizationName() : "-");

            addDocumentHeader(document, "RELATÓRIO MACRO-GERENCIAL DE PRODUÇÃO",
                    "Escopo: " + scopeLabel,
                    "Filtro Comunidade: " + (data.appliedCommunityId() != null ? data.appliedCommunityId().toString() : "Todas"),
                    getHarvestLabel(data.currentHarvest() != null ? data.currentHarvest().label() : null));

            // KPI Cards
            addKpiRow(document, List.of(
                    new Kpi("Organizações", String.valueOf(data.totalOrganizationsCount())),
                    new Kpi("Comunidades", String.valueOf(data.totalCommunitiesCount())),
                    new Kpi("Produtores Ativos", String.valueOf(data.totalFarmersCount())),
                    new Kpi("Taxa Adimplência", formatPct(data.complianceRatePct())),
                    new Kpi("Planos Totais", String.valueOf(data.totalPlansCount())),
                    new Kpi("Área Total (ha)", formatHa(data.totalPlantedAreaHa())),
                    new Kpi("Meta Prevista", formatKg(data.totalTargetKg())),
                    new Kpi("Taxa Conversão", formatPct(data.overallConversionRatePct()))
            ));

            addSpacer(document, 8);
            addSectionTitle(document, "Ranking de Culturas");
            if (data.cropRanking() == null || data.cropRanking().isEmpty()) {
                addEmptyStateNotice(document, "Nenhum registro de produção por cultura nesta safra.");
            } else {
                addManagementCropRankingTable(document, data.cropRanking());
            }

            addSpacer(document, 8);
            addSectionTitle(document, "Desempenho por Comunidade");
            if (data.communities() == null || data.communities().isEmpty()) {
                addEmptyStateNotice(document, "Nenhum registro de comunidade nesta safra.");
            } else {
                addManagementCommunitiesTable(document, data.communities(), data.scope());
            }

            if (data.scope() == ManagementDashboardScope.GLOBAL && data.organizations() != null && !data.organizations().isEmpty()) {
                addSpacer(document, 8);
                addSectionTitle(document, "Comparativo por Organização");
                addManagementOrganizationsTable(document, data.organizations());
            }
        });
    }

    // ==========================================
    // Tabelas Específicas
    // ==========================================

    private void addFarmerPlansTable(Document document, List<FarmerPlanSummaryCardDTO> plans) throws DocumentException {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{16, 12, 10, 10, 11, 11, 10, 10, 10});

        addHeaderCell(table, "Cultura / Variedade");
        addHeaderCell(table, "Talhão / Tipo");
        addHeaderCell(table, "Área (ha)");
        addHeaderCell(table, "Meta (kg)");
        addHeaderCell(table, "Realizado (kg)");
        addHeaderCell(table, "Pendente (kg)");
        addHeaderCell(table, "% Conclusão");
        addHeaderCell(table, "Progresso");
        addHeaderCell(table, "Status");

        boolean zebra = false;
        for (FarmerPlanSummaryCardDTO p : plans) {
            Color rowBg = zebra ? COLOR_ZEBRA_BG : Color.WHITE;
            zebra = !zebra;

            String cropInfo = p.cropName() + (p.cropVariety() != null ? " (" + p.cropVariety() + ")" : "");
            String locationInfo = (p.locationDescription() != null ? p.locationDescription() : "-") +
                    (p.harvestType() != null ? " [" + p.harvestType().name() + "]" : "");

            addDataCell(table, cropInfo, Element.ALIGN_LEFT, rowBg);
            addDataCell(table, locationInfo, Element.ALIGN_LEFT, rowBg);
            addDataCell(table, formatHa(p.plantedAreaHa()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(p.targetKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(p.realizedKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(p.pendingKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatPct(p.completionPct()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, p.progressStatus() != null ? p.progressStatus().name() : "-", Element.ALIGN_CENTER, rowBg);
            addDataCell(table, p.status() != null ? p.status().name() : "-", Element.ALIGN_CENTER, rowBg);
        }

        document.add(table);
    }

    private void addTechnicianCommunitiesTable(Document document, List<TechnicianCommunitySummaryDTO> communities) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{22, 11, 11, 11, 13, 13, 11, 8});

        addHeaderCell(table, "Comunidade");
        addHeaderCell(table, "Produtores");
        addHeaderCell(table, "Com Planos");
        addHeaderCell(table, "Planos");
        addHeaderCell(table, "Área (ha)");
        addHeaderCell(table, "Realizado (kg)");
        addHeaderCell(table, "% Conclusão");
        addHeaderCell(table, "Alertas");

        boolean zebra = false;
        for (TechnicianCommunitySummaryDTO c : communities) {
            Color rowBg = zebra ? COLOR_ZEBRA_BG : Color.WHITE;
            zebra = !zebra;

            addDataCell(table, c.communityName(), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, String.valueOf(c.assistedFarmersCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, String.valueOf(c.farmersWithPlansCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, String.valueOf(c.plansCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, formatHa(c.plantedAreaHa()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(c.realizedKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatPct(c.completionPct()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, String.valueOf(c.warningsCount()), Element.ALIGN_CENTER, rowBg);
        }

        document.add(table);
    }

    private void addTechnicianFarmersTable(Document document, List<TechnicianFarmerSummaryDTO> farmers) throws DocumentException {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{18, 16, 10, 8, 10, 12, 12, 8, 6});

        addHeaderCell(table, "Agricultor");
        addHeaderCell(table, "Comunidade");
        addHeaderCell(table, "Situação");
        addHeaderCell(table, "Planos");
        addHeaderCell(table, "Área (ha)");
        addHeaderCell(table, "Meta (kg)");
        addHeaderCell(table, "Realizado (kg)");
        addHeaderCell(table, "% Concl.");
        addHeaderCell(table, "Alertas");

        boolean zebra = false;
        for (TechnicianFarmerSummaryDTO f : farmers) {
            Color rowBg = zebra ? COLOR_ZEBRA_BG : Color.WHITE;
            zebra = !zebra;

            addDataCell(table, f.farmerName(), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, f.communityName(), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, f.isCompliant() ? "Adimplente" : "Inadimplente", Element.ALIGN_CENTER, rowBg);
            addDataCell(table, f.activePlansCount() + "/" + f.plansCount(), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, formatHa(f.plantedAreaHa()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(f.targetKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(f.realizedKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatPct(f.completionPct()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, String.valueOf(f.warningsCount()), Element.ALIGN_CENTER, rowBg);
        }

        document.add(table);
    }

    private void addTechnicianWarningsTable(Document document, List<TechnicianPlanWarningDTO> warnings) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 18, 16, 16, 28, 12});

        addHeaderCell(table, "Severidade");
        addHeaderCell(table, "Agricultor");
        addHeaderCell(table, "Comunidade");
        addHeaderCell(table, "Cultura");
        addHeaderCell(table, "Mensagem / Detalhe");
        addHeaderCell(table, "% Conclusão");

        boolean zebra = false;
        for (TechnicianPlanWarningDTO w : warnings) {
            Color rowBg = zebra ? COLOR_ZEBRA_BG : Color.WHITE;
            zebra = !zebra;

            String sev = w.severity() != null ? w.severity().name() : "WARNING";
            Color sevColor = "CRITICAL".equalsIgnoreCase(sev) ? COLOR_CRITICAL : COLOR_WARNING;

            PdfPCell sevCell = new PdfPCell(new Phrase(sev, new Font(Font.HELVETICA, 7, Font.BOLD, sevColor)));
            sevCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            sevCell.setBackgroundColor(rowBg);
            sevCell.setPadding(4);
            sevCell.setBorderColor(COLOR_BORDER);
            table.addCell(sevCell);

            addDataCell(table, w.farmerName(), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, w.communityName(), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, w.cropName() + (w.cropVariety() != null ? " (" + w.cropVariety() + ")" : ""), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, w.message(), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, formatPct(w.completionPct()), Element.ALIGN_RIGHT, rowBg);
        }

        document.add(table);
    }

    private void addManagementCropRankingTable(Document document, List<ManagementCropRankingItemDTO> ranking) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{6, 20, 10, 11, 14, 14, 12, 13});

        addHeaderCell(table, "Pos.");
        addHeaderCell(table, "Cultura / Variedade");
        addHeaderCell(table, "Planos");
        addHeaderCell(table, "Área (ha)");
        addHeaderCell(table, "Meta (kg)");
        addHeaderCell(table, "Realizado (kg)");
        addHeaderCell(table, "% Conversão");
        addHeaderCell(table, "% Share");

        boolean zebra = false;
        for (ManagementCropRankingItemDTO c : ranking) {
            Color rowBg = zebra ? COLOR_ZEBRA_BG : Color.WHITE;
            zebra = !zebra;

            String cropInfo = c.cropName() + (c.cropVariety() != null ? " (" + c.cropVariety() + ")" : "");
            addDataCell(table, "#" + c.rankPosition(), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, cropInfo, Element.ALIGN_LEFT, rowBg);
            addDataCell(table, String.valueOf(c.plansCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, formatHa(c.plantedAreaHa()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(c.targetKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(c.realizedKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatPct(c.conversionRatePct()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatPct(c.productionSharePct()), Element.ALIGN_RIGHT, rowBg);
        }

        document.add(table);
    }

    private void addManagementCommunitiesTable(Document document, List<ManagementCommunityPerformanceDTO> communities, ManagementDashboardScope scope) throws DocumentException {
        boolean showOrg = scope == ManagementDashboardScope.GLOBAL;
        PdfPTable table = new PdfPTable(showOrg ? 8 : 7);
        table.setWidthPercentage(100);

        if (showOrg) {
            table.setWidths(new float[]{18, 16, 11, 11, 11, 12, 11, 10});
        } else {
            table.setWidths(new float[]{22, 13, 13, 13, 14, 13, 12});
        }

        addHeaderCell(table, "Comunidade");
        if (showOrg) {
            addHeaderCell(table, "Organização");
        }
        addHeaderCell(table, "Prod. Ativos");
        addHeaderCell(table, "Adimplentes");
        addHeaderCell(table, "% Adimpl.");
        addHeaderCell(table, "Área (ha)");
        addHeaderCell(table, "Realizado (kg)");
        addHeaderCell(table, "% Conversão");

        boolean zebra = false;
        for (ManagementCommunityPerformanceDTO c : communities) {
            Color rowBg = zebra ? COLOR_ZEBRA_BG : Color.WHITE;
            zebra = !zebra;

            addDataCell(table, c.communityName(), Element.ALIGN_LEFT, rowBg);
            if (showOrg) {
                addDataCell(table, c.organizationName() != null ? c.organizationName() : "-", Element.ALIGN_LEFT, rowBg);
            }
            addDataCell(table, String.valueOf(c.activeFarmersCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, String.valueOf(c.compliantFarmersCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, formatPct(c.complianceRatePct()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatHa(c.plantedAreaHa()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(c.realizedKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatPct(c.conversionRatePct()), Element.ALIGN_RIGHT, rowBg);
        }

        document.add(table);
    }

    private void addManagementOrganizationsTable(Document document, List<ManagementOrganizationSummaryDTO> orgs) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{22, 11, 11, 11, 11, 12, 12, 10});

        addHeaderCell(table, "Organização");
        addHeaderCell(table, "Comunidades");
        addHeaderCell(table, "Produtores");
        addHeaderCell(table, "Adimplentes");
        addHeaderCell(table, "% Adimpl.");
        addHeaderCell(table, "Área (ha)");
        addHeaderCell(table, "Realizado (kg)");
        addHeaderCell(table, "% Conversão");

        boolean zebra = false;
        for (ManagementOrganizationSummaryDTO o : orgs) {
            Color rowBg = zebra ? COLOR_ZEBRA_BG : Color.WHITE;
            zebra = !zebra;

            addDataCell(table, o.organizationName(), Element.ALIGN_LEFT, rowBg);
            addDataCell(table, String.valueOf(o.communitiesCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, String.valueOf(o.activeFarmersCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, String.valueOf(o.compliantFarmersCount()), Element.ALIGN_CENTER, rowBg);
            addDataCell(table, formatPct(o.complianceRatePct()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatHa(o.plantedAreaHa()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatKg(o.realizedKg()), Element.ALIGN_RIGHT, rowBg);
            addDataCell(table, formatPct(o.conversionRatePct()), Element.ALIGN_RIGHT, rowBg);
        }

        document.add(table);
    }

    // ==========================================
    // Helpers de Layout e Renderização
    // ==========================================

    @FunctionalInterface
    private interface DocumentConsumer {
        void accept(Document document) throws Exception;
    }

    private byte[] buildPdf(DocumentConsumer consumer) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 25, 25);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new SmartAgroPageFooter());

            document.open();
            consumer.accept(document);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar relatório PDF", e);
        }
    }

    private void addDocumentHeader(Document document, String title, String meta1, String meta2, String meta3) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{65, 35});

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.addElement(new Paragraph("SMARTAGRO PLATFORM", FONT_TITLE));
        titleCell.addElement(new Paragraph(title, FONT_SUBTITLE));
        headerTable.addCell(titleCell);

        PdfPCell metaCell = new PdfPCell();
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaCell.addElement(new Paragraph(meta1, FONT_META));
        metaCell.addElement(new Paragraph(meta2, FONT_META));
        metaCell.addElement(new Paragraph(meta3, FONT_META));
        metaCell.addElement(new Paragraph("Gerado em: " + LocalDateTime.now().format(DATE_TIME_FORMATTER), FONT_META));
        headerTable.addCell(metaCell);

        document.add(headerTable);
        addDivider(document);
    }

    private void addDivider(Document document) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        line.setSpacingBefore(4);
        line.setSpacingAfter(8);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setFixedHeight(2f);
        cell.setBorder(Rectangle.NO_BORDER);
        line.addCell(cell);
        document.add(line);
    }

    private void addKpiRow(Document document, List<Kpi> kpis) throws DocumentException {
        if (kpis == null || kpis.isEmpty()) return;

        PdfPTable table = new PdfPTable(kpis.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(6);

        for (Kpi kpi : kpis) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(COLOR_PRIMARY_LIGHT);
            cell.setBorderColor(COLOR_BORDER);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph label = new Paragraph(kpi.label().toUpperCase(), FONT_KPI_LABEL);
            label.setAlignment(Element.ALIGN_CENTER);
            Paragraph value = new Paragraph(kpi.value(), FONT_KPI_VALUE);
            value.setAlignment(Element.ALIGN_CENTER);

            cell.addElement(label);
            cell.addElement(value);
            table.addCell(cell);
        }

        document.add(table);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, FONT_SECTION);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        document.add(p);
    }

    private void addSpacer(Document document, float height) throws DocumentException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingBefore(height);
        document.add(spacer);
    }

    private void addEmptyStateNotice(Document document, String message) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        PdfPCell cell = new PdfPCell(new Phrase(message, FONT_META));
        cell.setBackgroundColor(COLOR_ZEBRA_BG);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLE_HEADER));
        cell.setBackgroundColor(COLOR_HEADER_BG);
        cell.setBorderColor(COLOR_HEADER_BG);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, int align, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", FONT_TABLE_CELL));
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(4);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String getHarvestLabel(String harvestLabel) {
        return "Safra: " + (harvestLabel != null && !harvestLabel.isBlank() ? harvestLabel : "Safra Vigente");
    }

    private String formatHa(BigDecimal value) {
        if (value == null) return "0,00 ha";
        return String.format(LOCALE_BR, "%,.2f ha", value);
    }

    private String formatKg(BigDecimal value) {
        if (value == null) return "0,00 kg";
        return String.format(LOCALE_BR, "%,.2f kg", value);
    }

    private String formatPct(BigDecimal value) {
        if (value == null) return "0,00%";
        return String.format(LOCALE_BR, "%.2f%%", value);
    }

    private record Kpi(String label, String value) {}

    /**
     * Rodapé padronizado com numeração e informações legais.
     */
    private static class SmartAgroPageFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            footer.setTotalWidth(document.right() - document.left());
            footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell left = new PdfPCell(new Phrase("SmartAgro Analytics - Documento confidencial para uso exclusivo.", FONT_FOOTER));
            left.setBorder(Rectangle.NO_BORDER);
            footer.addCell(left);

            PdfPCell right = new PdfPCell(new Phrase("Página " + writer.getPageNumber(), FONT_FOOTER));
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            right.setBorder(Rectangle.NO_BORDER);
            footer.addCell(right);

            footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 8, writer.getDirectContent());
        }
    }
}
