package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.application.service.production.GetHarvestTimelineUseCase;
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
}
