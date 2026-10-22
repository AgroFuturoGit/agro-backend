package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.application.service.production.GetFarmerDashboardSummaryUseCase;
import com.ufal.smartagro.application.service.production.GetHarvestTimelineUseCase;
import com.ufal.smartagro.application.service.production.GetManagementDashboardSummaryUseCase;
import com.ufal.smartagro.application.service.production.GetTechnicianDashboardSummaryUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;
import com.ufal.smartagro.domain.port.out.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final UserRepository userRepository;

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

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

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
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        FarmerDashboardSummaryDTO summary = getFarmerDashboardSummaryUseCase.execute(harvestId, loggedUser);
        return ResponseEntity.ok(summary);
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
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        TechnicianDashboardSummaryDTO summary = getTechnicianDashboardSummaryUseCase
                .execute(harvestId, communityId, cropId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Dashboard macro-gerencial da organização do gestor autenticado",
            description = "Consolida os indicadores de desempenho, ranking de culturas e comparativo de comunidades da organização do gestor logado.",
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
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeForCurrentManager(harvestId, communityId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Dashboard macro-gerencial de uma organização específica",
            description = "Consolida os indicadores de desempenho de uma organização. Administradores podem consultar qualquer organização; Gestores apenas a sua.",
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
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeForOrganization(organizationId, harvestId, communityId, loggedUser);
        return ResponseEntity.ok(summary);
    }

    @Operation(
            summary = "Dashboard macro-gerencial global do sistema (Visão do Administrador)",
            description = "Consolida todos os dados produtivos e cadastrais do sistema em nível nacional/estadual, com comparativo entre organizações.",
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
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ManagementDashboardSummaryDTO summary = getManagementDashboardSummaryUseCase
                .executeGlobal(harvestId, organizationId, communityId, loggedUser);
        return ResponseEntity.ok(summary);
    }
}
