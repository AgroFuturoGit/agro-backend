package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.application.service.analytics.ReportExportService;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.GetFarmerDashboardSummaryUseCase;
import com.ufal.smartagro.application.service.production.GetHarvestTimelineUseCase;
import com.ufal.smartagro.application.service.production.GetManagementDashboardSummaryUseCase;
import com.ufal.smartagro.application.service.production.GetTechnicianDashboardSummaryUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.AuditAction;
import com.ufal.smartagro.domain.model.enums.ReportExportFormat;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Analytics", description = "Relatórios e visualizações analíticas de produção e colheita")
@RequiredArgsConstructor
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final GetHarvestTimelineUseCase getHarvestTimelineUseCase;
    private final GetFarmerDashboardSummaryUseCase getFarmerDashboardSummaryUseCase;
    private final GetTechnicianDashboardSummaryUseCase getTechnicianDashboardSummaryUseCase;
    private final GetManagementDashboardSummaryUseCase getManagementDashboardSummaryUseCase;
    private final ReportExportService reportExportService;
    private final AuditLogService auditLogService;

    @Operation(
            summary = "Evolução temporal comparativa da colheita",
            description = "Retorna a linha do tempo com a meta prevista, colheita validada e pendente para o plano informado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Linha do tempo gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Granularidade inválida (aceita apenas WEEK ou MONTH)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Plano de produção não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/plans/{planId}/timeline")
    public ResponseEntity<HarvestTimelineDTO> getHarvestTimeline(
            @PathVariable UUID planId,
            @Parameter(description = "Granularidade temporal: WEEK (padrão) ou MONTH", example = "WEEK")
            @RequestParam(name = "granularity", required = false, defaultValue = "WEEK") String granularity,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        TimelineGranularity parsedGranularity = TimelineGranularity.fromString(granularity);

        User loggedUser = loggedUserDetails.toDomainUser();

        HarvestTimelineDTO timeline = getHarvestTimelineUseCase.execute(planId, parsedGranularity, loggedUser);
        return ResponseEntity.ok(timeline);
    }

    @Operation(
            summary = "Resumo consolidado do dashboard individual do agricultor",
            description = "Retorna totais consolidados da safra (área, meta, colheita validada e pendente) e lista de cartões por plano com status de progresso dinâmico.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo analítico do agricultor retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (apenas agricultores possuem acesso)"),
            @ApiResponse(responseCode = "404", description = "Safra informada não encontrada")
    })
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/farmers/me/summary")
    public ResponseEntity<FarmerDashboardSummaryDTO> getFarmerDashboardSummary(
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        User loggedUser = loggedUserDetails.toDomainUser();

        FarmerDashboardSummaryDTO summary = getFarmerDashboardSummaryUseCase.execute(harvestId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Exportação de relatório analítico individual do agricultor (PDF ou XLSX)",
            description = "Gera e exporta relatório em arquivo binário PDF institucional ou planilha Excel (.xlsx) com preservação de tipos numéricos (§12 Modelagem v2.1). Registra trilha de auditoria EXPORT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo de relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de arquivo inválido (apenas 'pdf' ou 'xlsx')"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Safra informada não encontrada")
    })
    @PreAuthorize("hasRole('FARMER')")
    @Transactional
    @GetMapping("/farmers/me/summary/export")
    public ResponseEntity<byte[]> exportFarmerDashboardSummary(
            @Parameter(description = "Formato de exportação desejado: 'pdf' (padrão) ou 'xlsx'", example = "pdf")
            @RequestParam(name = "format", required = false, defaultValue = "pdf") String format,
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        ReportExportFormat exportFormat = ReportExportFormat.fromString(format);
        User loggedUser = loggedUserDetails.toDomainUser();

        FarmerDashboardSummaryDTO summary = getFarmerDashboardSummaryUseCase.execute(harvestId, loggedUser);

        byte[] reportBytes = exportFormat == ReportExportFormat.PDF
                ? reportExportService.exportFarmerSummaryToPdf(summary)
                : reportExportService.exportFarmerSummaryToXlsx(summary);

        Map<String, Object> details = new HashMap<>();
        details.put("reportType", "FARMER_SUMMARY");
        details.put("format", exportFormat.name());
        if (harvestId != null) {
            details.put("harvestId", harvestId.toString());
        }
        auditLogService.record(loggedUser, AuditAction.EXPORT, "analytics_reports", null, details);

        String filename = "relatorio-agricultor-" + sanitizeFilename(summary.farmerName()) + "." + exportFormat.getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportFormat.getMediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportBytes);
    }

    @Operation(
            summary = "Resumo consolidado do dashboard agregado do técnico",
            description = "Retorna indicadores macro da carteira, motor de alertas operacionais e agregações por comunidade e por agricultor assistido.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo analítico do técnico retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (apenas técnicos com assistência ativa possuem acesso)"),
            @ApiResponse(responseCode = "404", description = "Safra ou cultura informada não encontrada")
    })
    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping("/technicians/me/summary")
    public ResponseEntity<TechnicianDashboardSummaryDTO> getTechnicianDashboardSummary(
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da comunidade assistida para restringir a agregação.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @Parameter(description = "ID opcional da cultura para restringir a agregação.")
            @RequestParam(name = "cropId", required = false) UUID cropId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        User loggedUser = loggedUserDetails.toDomainUser();

        TechnicianDashboardSummaryDTO summary = getTechnicianDashboardSummaryUseCase
                .execute(harvestId, communityId, cropId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Exportação de relatório operacional da carteira técnica (PDF ou XLSX)",
            description = "Gera e exporta relatório da carteira técnica com abas de comunidades, agricultores e alertas operacionais. Registra trilha de auditoria EXPORT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo de relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de arquivo inválido (apenas 'pdf' ou 'xlsx')"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Safra ou cultura informada não encontrada")
    })
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Transactional
    @GetMapping("/technicians/me/summary/export")
    public ResponseEntity<byte[]> exportTechnicianDashboardSummary(
            @Parameter(description = "Formato de exportação desejado: 'pdf' (padrão) ou 'xlsx'", example = "pdf")
            @RequestParam(name = "format", required = false, defaultValue = "pdf") String format,
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da comunidade assistida para restringir a agregação.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @Parameter(description = "ID opcional da cultura para restringir a agregação.")
            @RequestParam(name = "cropId", required = false) UUID cropId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        ReportExportFormat exportFormat = ReportExportFormat.fromString(format);
        User loggedUser = loggedUserDetails.toDomainUser();

        TechnicianDashboardSummaryDTO summary = getTechnicianDashboardSummaryUseCase
                .execute(harvestId, communityId, cropId, loggedUser);

        byte[] reportBytes = exportFormat == ReportExportFormat.PDF
                ? reportExportService.exportTechnicianSummaryToPdf(summary)
                : reportExportService.exportTechnicianSummaryToXlsx(summary);

        Map<String, Object> details = new HashMap<>();
        details.put("reportType", "TECHNICIAN_SUMMARY");
        details.put("format", exportFormat.name());
        if (harvestId != null) details.put("harvestId", harvestId.toString());
        if (communityId != null) details.put("communityId", communityId.toString());
        if (cropId != null) details.put("cropId", cropId.toString());

        auditLogService.record(loggedUser, AuditAction.EXPORT, "analytics_reports", null, details);

        String filename = "relatorio-carteira-tecnica-" + sanitizeFilename(summary.technicianName()) + "." + exportFormat.getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportFormat.getMediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportBytes);
    }

    @Operation(
            summary = "Dashboard macro-gerencial da organização do gestor autenticado",
            description = "Consolida os indicadores de desempenho, ranking de culturas e comparativo de comunidades da organização do gestor logado a partir da visão materializada 'mv_org_harvest_summary' (com janela de atualização programada de até 5 minutos).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo analítico macro-gerencial retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (apenas gestores com organização vinculada possuem acesso)"),
            @ApiResponse(responseCode = "404", description = "Safra ou comunidade informada não encontrada")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/organizations/me/summary")
    public ResponseEntity<ManagementDashboardSummaryDTO> getCurrentManagerDashboardSummary(
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da comunidade para filtrar o desempenho.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        User loggedUser = loggedUserDetails.toDomainUser();

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeForCurrentManager(harvestId, communityId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Exportação de relatório macro-gerencial da organização do gestor (PDF ou XLSX)",
            description = "Gera e exporta relatório da organização com ranking de culturas e desempenho por comunidade a partir da visão materializada 'mv_org_harvest_summary' (janela de atualização de até 5 minutos). Registra trilha de auditoria EXPORT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo de relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de arquivo inválido (apenas 'pdf' ou 'xlsx')"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Safra ou comunidade informada não encontrada")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    @GetMapping("/organizations/me/summary/export")
    public ResponseEntity<byte[]> exportCurrentManagerDashboardSummary(
            @Parameter(description = "Formato de exportação desejado: 'pdf' (padrão) ou 'xlsx'", example = "pdf")
            @RequestParam(name = "format", required = false, defaultValue = "pdf") String format,
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da comunidade para filtrar o desempenho.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        ReportExportFormat exportFormat = ReportExportFormat.fromString(format);
        User loggedUser = loggedUserDetails.toDomainUser();

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeForCurrentManager(harvestId, communityId, loggedUser);

        byte[] reportBytes = exportFormat == ReportExportFormat.PDF
                ? reportExportService.exportManagementSummaryToPdf(summary)
                : reportExportService.exportManagementSummaryToXlsx(summary);

        Map<String, Object> details = new HashMap<>();
        details.put("reportType", "MANAGEMENT_ORGANIZATION_SUMMARY");
        details.put("format", exportFormat.name());
        if (summary.organizationId() != null) details.put("organizationId", summary.organizationId().toString());
        if (harvestId != null) details.put("harvestId", harvestId.toString());
        if (communityId != null) details.put("communityId", communityId.toString());

        auditLogService.record(loggedUser, AuditAction.EXPORT, "analytics_reports", summary.organizationId(), details);

        String filename = "relatorio-organizacao-" + sanitizeFilename(summary.organizationName()) + "." + exportFormat.getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportFormat.getMediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportBytes);
    }

    @Operation(
            summary = "Dashboard macro-gerencial de uma organização específica",
            description = "Consolida os indicadores de desempenho de uma organização específica a partir da visão materializada 'mv_org_harvest_summary' (janela de atualização de até 5 minutos). Administradores podem consultar qualquer organização; Gestores apenas a sua.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo analítico retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (gestor tentando acessar outra organização)"),
            @ApiResponse(responseCode = "404", description = "Organização, safra ou comunidade informada não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/organizations/{organizationId}/summary")
    public ResponseEntity<ManagementDashboardSummaryDTO> getOrganizationDashboardSummary(
            @Parameter(description = "ID da organização consultada.", required = true)
            @PathVariable(name = "organizationId") UUID organizationId,
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da comunidade pertencente à organização para restringir a agregação.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        User loggedUser = loggedUserDetails.toDomainUser();

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeForOrganization(organizationId, harvestId, communityId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Exportação de relatório macro-gerencial de uma organização específica (PDF ou XLSX)",
            description = "Gera e exporta relatório da organização especificada a partir da visão materializada 'mv_org_harvest_summary' (janela de atualização de até 5 minutos). Administradores podem exportar qualquer organização; Gestores apenas a sua. Registra trilha de auditoria EXPORT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo de relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de arquivo inválido (apenas 'pdf' ou 'xlsx')"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Organização, safra ou comunidade informada não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional
    @GetMapping("/organizations/{organizationId}/summary/export")
    public ResponseEntity<byte[]> exportOrganizationDashboardSummary(
            @Parameter(description = "ID da organização consultada.", required = true)
            @PathVariable(name = "organizationId") UUID organizationId,
            @Parameter(description = "Formato de exportação desejado: 'pdf' (padrão) ou 'xlsx'", example = "pdf")
            @RequestParam(name = "format", required = false, defaultValue = "pdf") String format,
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da comunidade pertencente à organização para restringir a agregação.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        ReportExportFormat exportFormat = ReportExportFormat.fromString(format);
        User loggedUser = loggedUserDetails.toDomainUser();

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeForOrganization(organizationId, harvestId, communityId, loggedUser);

        byte[] reportBytes = exportFormat == ReportExportFormat.PDF
                ? reportExportService.exportManagementSummaryToPdf(summary)
                : reportExportService.exportManagementSummaryToXlsx(summary);

        Map<String, Object> details = new HashMap<>();
        details.put("reportType", "MANAGEMENT_ORGANIZATION_SUMMARY");
        details.put("format", exportFormat.name());
        details.put("organizationId", organizationId.toString());
        if (harvestId != null) details.put("harvestId", harvestId.toString());
        if (communityId != null) details.put("communityId", communityId.toString());

        auditLogService.record(loggedUser, AuditAction.EXPORT, "analytics_reports", organizationId, details);

        String filename = "relatorio-organizacao-" + sanitizeFilename(summary.organizationName()) + "." + exportFormat.getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportFormat.getMediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportBytes);
    }

    @Operation(
            summary = "Dashboard macro-gerencial global do sistema (Visão do Administrador)",
            description = "Consolida todos os dados produtivos e cadastrais do sistema em nível nacional/estadual a partir da visão materializada 'mv_org_harvest_summary' (janela de atualização de até 5 minutos). Quando o parâmetro opcional 'organizationId' for informado para filtrar uma organização na visão do administrador, o campo 'scope' retornado no payload permanece 'GLOBAL', refletindo o contexto de autorização administrativa da sessão.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo analítico global retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (apenas administradores globais possuem acesso)"),
            @ApiResponse(responseCode = "404", description = "Organização, safra ou comunidade informada não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/global/summary")
    public ResponseEntity<ManagementDashboardSummaryDTO> getGlobalDashboardSummary(
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da organização para filtrar os dados na visão de administrador.")
            @RequestParam(name = "organizationId", required = false) UUID organizationId,
            @Parameter(description = "ID opcional da comunidade para filtrar os dados na visão de administrador.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        User loggedUser = loggedUserDetails.toDomainUser();

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeGlobal(harvestId, organizationId, communityId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Exportação de relatório macro-gerencial global do sistema (PDF ou XLSX)",
            description = "Gera e exporta relatório gerencial global com consolidação nacional, ranking de culturas e comparativo de organizações a partir da visão materializada 'mv_org_harvest_summary' (janela de atualização de até 5 minutos). Quando filtrado por 'organizationId', o escopo documentado e exportado permanece 'GLOBAL'. Registra trilha de auditoria EXPORT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo de relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato de arquivo inválido (apenas 'pdf' ou 'xlsx')"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (apenas administradores globais possuem acesso)"),
            @ApiResponse(responseCode = "404", description = "Organização, safra ou comunidade informada não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @GetMapping("/global/summary/export")
    public ResponseEntity<byte[]> exportGlobalDashboardSummary(
            @Parameter(description = "Formato de exportação desejado: 'pdf' (padrão) ou 'xlsx'", example = "pdf")
            @RequestParam(name = "format", required = false, defaultValue = "pdf") String format,
            @Parameter(description = "ID opcional da safra desejada. Se omitido, busca automaticamente a safra vigente.")
            @RequestParam(name = "harvestId", required = false) UUID harvestId,
            @Parameter(description = "ID opcional da organização para filtrar os dados na visão de administrador.")
            @RequestParam(name = "organizationId", required = false) UUID organizationId,
            @Parameter(description = "ID opcional da comunidade para filtrar os dados na visão de administrador.")
            @RequestParam(name = "communityId", required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails
    ) {
        ReportExportFormat exportFormat = ReportExportFormat.fromString(format);
        User loggedUser = loggedUserDetails.toDomainUser();

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeGlobal(harvestId, organizationId, communityId, loggedUser);

        byte[] reportBytes = exportFormat == ReportExportFormat.PDF
                ? reportExportService.exportManagementSummaryToPdf(summary)
                : reportExportService.exportManagementSummaryToXlsx(summary);

        Map<String, Object> details = new HashMap<>();
        details.put("reportType", "MANAGEMENT_GLOBAL_SUMMARY");
        details.put("format", exportFormat.name());
        if (harvestId != null) details.put("harvestId", harvestId.toString());
        if (organizationId != null) details.put("organizationId", organizationId.toString());
        if (communityId != null) details.put("communityId", communityId.toString());

        auditLogService.record(loggedUser, AuditAction.EXPORT, "analytics_reports", null, details);

        String filename = "relatorio-gerencial-global." + exportFormat.getExtension();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportFormat.getMediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportBytes);
    }

    private String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) {
            return "dados";
        }
        String normalized = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9-_]", "_")
                .replaceAll("_+", "_");
        return normalized.toLowerCase(Locale.ROOT);
    }
}
