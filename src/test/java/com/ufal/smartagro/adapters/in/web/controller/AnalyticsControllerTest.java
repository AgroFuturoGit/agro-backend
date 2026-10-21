package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerPlanSummaryCardDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestSummaryInfoDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TimelinePointDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.production.GetFarmerDashboardSummaryUseCase;
import com.ufal.smartagro.application.service.production.GetHarvestTimelineUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@Import(SecurityConfig.class)
@DisplayName("AnalyticsController — endpoint de linha do tempo de colheita")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetHarvestTimelineUseCase getHarvestTimelineUseCase;

    @MockitoBean
    private GetFarmerDashboardSummaryUseCase getFarmerDashboardSummaryUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User adminUser;
    private UserDetailsImpl adminUserDetails;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        adminUser = UserTestFactory.user().role(Role.ADMIN).build();
        adminUserDetails = userDetails(adminUser);
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
    }

    private UserDetailsImpl userDetails(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setRole(user.getRole());
        return new UserDetailsImpl(entity);
    }

    @Test
    @DisplayName("Deve retornar HTTP 200 com payload JSON válido para granularidade padrão (WEEK)")
    void deveRetornarHttp200ComPayloadJsonValido() throws Exception {
        UUID planId = UUID.randomUUID();
        TimelinePointDTO point = new TimelinePointDTO(
                LocalDate.of(2026, 5, 4),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                new BigDecimal("20.00")
        );
        HarvestTimelineDTO dto = new HarvestTimelineDTO(
                planId,
                HarvestType.CONTINUOUS,
                TimelineGranularity.WEEK,
                "kg",
                List.of(point)
        );

        when(getHarvestTimelineUseCase.execute(eq(planId), eq(TimelineGranularity.WEEK), eq(adminUser)))
                .thenReturn(dto);

        mockMvc.perform(get("/analytics/plans/{planId}/timeline", planId)
                        .with(user(adminUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value(planId.toString()))
                .andExpect(jsonPath("$.harvestType").value("CONTINUOUS"))
                .andExpect(jsonPath("$.granularity").value("WEEK"))
                .andExpect(jsonPath("$.unit").value("kg"))
                .andExpect(jsonPath("$.points[0].date").value("2026-05-04"))
                .andExpect(jsonPath("$.points[0].expectedValue").value(100.00))
                .andExpect(jsonPath("$.points[0].actualValue").value(50.00))
                .andExpect(jsonPath("$.points[0].pendingValue").value(20.00));
    }

    @Test
    @DisplayName("Deve aceitar parâmetro granularity case-insensitive (week e month)")
    void deveAceitarGranularidadeCaseInsensitive() throws Exception {
        UUID planId = UUID.randomUUID();
        HarvestTimelineDTO weekDto = new HarvestTimelineDTO(planId, HarvestType.CONTINUOUS, TimelineGranularity.WEEK, List.of());
        HarvestTimelineDTO monthDto = new HarvestTimelineDTO(planId, HarvestType.CONTINUOUS, TimelineGranularity.MONTH, List.of());

        when(getHarvestTimelineUseCase.execute(eq(planId), eq(TimelineGranularity.WEEK), eq(adminUser)))
                .thenReturn(weekDto);
        when(getHarvestTimelineUseCase.execute(eq(planId), eq(TimelineGranularity.MONTH), eq(adminUser)))
                .thenReturn(monthDto);

        mockMvc.perform(get("/analytics/plans/{planId}/timeline", planId)
                        .param("granularity", "week")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularity").value("WEEK"));

        mockMvc.perform(get("/analytics/plans/{planId}/timeline", planId)
                        .param("granularity", "month")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularity").value("MONTH"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 padronizado para granularidade inválida")
    void deveRetornarHttp400ParaGranularidadeInvalida() throws Exception {
        UUID planId = UUID.randomUUID();

        mockMvc.perform(get("/analytics/plans/{planId}/timeline", planId)
                        .param("granularity", "YEAR")
                        .with(user(adminUserDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Granularidade inválida: 'YEAR'. Valores permitidos: WEEK, MONTH."));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 quando o plano não for encontrado")
    void deveRetornarHttp404QuandoPlanoNaoEncontrado() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(getHarvestTimelineUseCase.execute(eq(nonExistentId), eq(TimelineGranularity.WEEK), eq(adminUser)))
                .thenThrow(new EntityNotFoundException("Plano de produção não encontrado."));

        mockMvc.perform(get("/analytics/plans/{planId}/timeline", nonExistentId)
                        .with(user(adminUserDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plano de produção não encontrado."));
    }

    @Test
    @DisplayName("GET /farmers/me/summary: deve retornar HTTP 200 com resumo consolidado do agricultor autenticado")
    void deveRetornarHttp200ComResumoDoAgricultorAutenticado() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        UUID harvestId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID cropId = UUID.randomUUID();

        HarvestSummaryInfoDTO harvestDto = new HarvestSummaryInfoDTO(
                harvestId, "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
        );

        FarmerPlanSummaryCardDTO cardDto = new FarmerPlanSummaryCardDTO(
                planId,
                cropId,
                "Mandioca",
                "Branca",
                HarvestType.SINGLE,
                "KG",
                PlanStatus.ACTIVE,
                "Talhão 1",
                new BigDecimal("2.50"),
                new BigDecimal("5000.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("70.00"),
                PlanProgressStatus.ON_TRACK,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 9, 15)
        );

        FarmerDashboardSummaryDTO summaryDto = new FarmerDashboardSummaryDTO(
                farmerUser.getId(),
                farmerUser.getFullName(),
                "Comunidade São Pedro",
                harvestDto,
                false,
                1,
                1,
                new BigDecimal("2.50"),
                new BigDecimal("5000.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("70.00"),
                List.of(cardDto)
        );

        when(getFarmerDashboardSummaryUseCase.execute(null, farmerUser)).thenReturn(summaryDto);

        mockMvc.perform(get("/analytics/farmers/me/summary")
                        .with(user(farmerUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmerId").value(farmerUser.getId().toString()))
                .andExpect(jsonPath("$.farmerName").value(farmerUser.getFullName()))
                .andExpect(jsonPath("$.communityName").value("Comunidade São Pedro"))
                .andExpect(jsonPath("$.currentHarvest.id").value(harvestId.toString()))
                .andExpect(jsonPath("$.currentHarvest.label").value("Safra 2026"))
                .andExpect(jsonPath("$.isEmpty").value(false))
                .andExpect(jsonPath("$.totalPlansCount").value(1))
                .andExpect(jsonPath("$.activePlotsCount").value(1))
                .andExpect(jsonPath("$.totalPlantedAreaHa").value(2.50))
                .andExpect(jsonPath("$.totalTargetKg").value(5000.00))
                .andExpect(jsonPath("$.totalRealizedKg").value(3500.00))
                .andExpect(jsonPath("$.totalPendingKg").value(500.00))
                .andExpect(jsonPath("$.overallCompletionPct").value(70.00))
                .andExpect(jsonPath("$.plans[0].planId").value(planId.toString()))
                .andExpect(jsonPath("$.plans[0].cropName").value("Mandioca"))
                .andExpect(jsonPath("$.plans[0].progressStatus").value("ON_TRACK"));
    }

    @Test
    @DisplayName("GET /farmers/me/summary: deve repassar harvestId quando fornecido como query param")
    void devePassarHarvestIdQuandoFornecido() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        UUID harvestId = UUID.randomUUID();
        FarmerDashboardSummaryDTO emptyDto = FarmerDashboardSummaryDTO.empty(
                farmerUser.getId(), farmerUser.getFullName(), null, null
        );

        when(getFarmerDashboardSummaryUseCase.execute(harvestId, farmerUser)).thenReturn(emptyDto);

        mockMvc.perform(get("/analytics/farmers/me/summary")
                        .param("harvestId", harvestId.toString())
                        .with(user(farmerUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEmpty").value(true));
    }

    @Test
    @DisplayName("GET /farmers/me/summary: deve retornar HTTP 404 quando harvestId informado não existir")
    void deveRetornarHttp404QuandoSafraNaoEncontrada() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        UUID nonExistentHarvestId = UUID.randomUUID();
        when(getFarmerDashboardSummaryUseCase.execute(nonExistentHarvestId, farmerUser))
                .thenThrow(new HarvestNotFoundException("Safra não encontrada."));

        mockMvc.perform(get("/analytics/farmers/me/summary")
                        .param("harvestId", nonExistentHarvestId.toString())
                        .with(user(farmerUserDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Safra não encontrada."));
    }

    @Test
    @DisplayName("GET /farmers/me/summary: deve retornar HTTP 403 quando usuário não possuir papel FARMER")
    void deveRetornarHttp403QuandoUsuarioNaoForFarmer() throws Exception {
        mockMvc.perform(get("/analytics/farmers/me/summary")
                        .with(user(adminUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /farmers/me/summary: deve rejeitar requisição não autenticada com HTTP 403")
    void deveRejeitarRequisicaoNaoAutenticada() throws Exception {
        mockMvc.perform(get("/analytics/farmers/me/summary"))
                .andExpect(status().isForbidden());
    }
}
