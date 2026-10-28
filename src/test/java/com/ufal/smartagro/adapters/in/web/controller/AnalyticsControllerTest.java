package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.FarmerPlanSummaryCardDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestSummaryInfoDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCommunityPerformanceDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementCropRankingItemDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.ManagementOrganizationSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianCommunitySummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianDashboardSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianFarmerSummaryDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TechnicianPlanWarningDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TimelinePointDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.analytics.ReportExportService;
import com.ufal.smartagro.application.service.audit.AuditLogService;
import com.ufal.smartagro.application.service.production.GetFarmerDashboardSummaryUseCase;
import com.ufal.smartagro.application.service.production.GetHarvestTimelineUseCase;
import com.ufal.smartagro.application.service.production.GetManagementDashboardSummaryUseCase;
import com.ufal.smartagro.application.service.production.GetTechnicianDashboardSummaryUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.PlanProgressStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.model.enums.TechnicianWarningType;
import com.ufal.smartagro.domain.model.enums.TimelineGranularity;
import com.ufal.smartagro.domain.model.enums.WarningSeverity;
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
import com.ufal.smartagro.domain.model.enums.AuditAction;
import org.springframework.http.HttpHeaders;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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
    private GetTechnicianDashboardSummaryUseCase getTechnicianDashboardSummaryUseCase;

    @MockitoBean
    private GetManagementDashboardSummaryUseCase getManagementDashboardSummaryUseCase;

    @MockitoBean
    private ReportExportService reportExportService;

    @MockitoBean
    private AuditLogService auditLogService;

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
    @DisplayName("GET /farmers/me/summary: deve rejeitar requisição não autenticada com HTTP 401")
    void deveRejeitarRequisicaoNaoAutenticada() throws Exception {
        mockMvc.perform(get("/analytics/farmers/me/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /technicians/me/summary: deve retornar HTTP 200 com resumo consolidado do técnico autenticado")
    void deveRetornarHttp200ComResumoDoTecnicoAutenticado() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        UUID techId = UUID.randomUUID();
        UUID commId = UUID.randomUUID();
        UUID farmerId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID cropId = UUID.randomUUID();

        HarvestSummaryInfoDTO harvestDto = new HarvestSummaryInfoDTO(
                UUID.randomUUID(), "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
        );

        TechnicianPlanWarningDTO warningDto = new TechnicianPlanWarningDTO(
                planId, farmerId, "Severino", commId, "Comunidade São José",
                cropId, "Tomate", "Cereja", PlanStatus.ACTIVE, PlanProgressStatus.BELOW_EXPECTED,
                List.of(TechnicianWarningType.INACTIVE_EXECUTIONS), WarningSeverity.HIGH,
                "Sem apontamentos há 19 dias",
                new BigDecimal("5000.00"), new BigDecimal("1450.00"), new BigDecimal("200.00"),
                new BigDecimal("2500.00"), new BigDecimal("29.00"),
                LocalDate.of(2026, 10, 2), 19, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 11, 30)
        );

        TechnicianCommunitySummaryDTO commDto = new TechnicianCommunitySummaryDTO(
                commId, "Comunidade São José", 8, 7, 10,
                new BigDecimal("18.00"), new BigDecimal("36000.00"), new BigDecimal("25200.00"),
                new BigDecimal("1800.00"), new BigDecimal("70.00"), 1
        );

        TechnicianFarmerSummaryDTO farmerDto = new TechnicianFarmerSummaryDTO(
                farmerId, "Severino", commId, "Comunidade São José", true,
                2, 2, new BigDecimal("4.50"), new BigDecimal("9000.00"), new BigDecimal("6450.00"),
                new BigDecimal("200.00"), new BigDecimal("71.67"), LocalDate.of(2026, 10, 2), 1
        );

        TechnicianDashboardSummaryDTO summaryDto = new TechnicianDashboardSummaryDTO(
                techId, "Carlos Eduardo Mendes", harvestDto, null, null, false,
                1, 1, 2, 2, new BigDecimal("4.50"), new BigDecimal("9000.00"),
                new BigDecimal("6450.00"), new BigDecimal("200.00"), 1, new BigDecimal("71.67"),
                1, List.of(warningDto), List.of(commDto), List.of(farmerDto)
        );

        when(getTechnicianDashboardSummaryUseCase.execute(null, null, null, technicianUser))
                .thenReturn(summaryDto);

        mockMvc.perform(get("/analytics/technicians/me/summary")
                        .with(user(technicianUserDetails))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianId").value(techId.toString()))
                .andExpect(jsonPath("$.technicianName").value("Carlos Eduardo Mendes"))
                .andExpect(jsonPath("$.currentHarvest.label").value("Safra 2026"))
                .andExpect(jsonPath("$.isEmpty").value(false))
                .andExpect(jsonPath("$.totalAssistedCommunitiesCount").value(1))
                .andExpect(jsonPath("$.totalAssistedFarmersCount").value(1))
                .andExpect(jsonPath("$.totalPlansCount").value(2))
                .andExpect(jsonPath("$.totalTargetKg").value(9000.00))
                .andExpect(jsonPath("$.warnings[0].planId").value(planId.toString()))
                .andExpect(jsonPath("$.warnings[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.communities[0].communityName").value("Comunidade São José"))
                .andExpect(jsonPath("$.farmers[0].farmerName").value("Severino"));
    }

    @Test
    @DisplayName("GET /technicians/me/summary: deve repassar harvestId, communityId e cropId quando fornecidos")
    void deveRepassarFiltrosOpcionaisParaOTecnico() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        UUID harvestId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        UUID cropId = UUID.randomUUID();

        TechnicianDashboardSummaryDTO emptyDto = TechnicianDashboardSummaryDTO.empty(
                technicianUser.getId(), technicianUser.getFullName(), null, communityId, cropId
        );

        when(getTechnicianDashboardSummaryUseCase.execute(harvestId, communityId, cropId, technicianUser))
                .thenReturn(emptyDto);

        mockMvc.perform(get("/analytics/technicians/me/summary")
                        .param("harvestId", harvestId.toString())
                        .param("communityId", communityId.toString())
                        .param("cropId", cropId.toString())
                        .with(user(technicianUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEmpty").value(true))
                .andExpect(jsonPath("$.appliedCommunityId").value(communityId.toString()))
                .andExpect(jsonPath("$.appliedCropId").value(cropId.toString()));
    }

    @Test
    @DisplayName("GET /technicians/me/summary: deve retornar HTTP 403 quando communityId não for assistida")
    void deveRetornarHttp403QuandoComunidadeNaoAssistida() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        UUID unassistedCommunityId = UUID.randomUUID();
        when(getTechnicianDashboardSummaryUseCase.execute(null, unassistedCommunityId, null, technicianUser))
                .thenThrow(new AccessDeniedException("Comunidade não assistida pelo técnico logado."));

        mockMvc.perform(get("/analytics/technicians/me/summary")
                        .param("communityId", unassistedCommunityId.toString())
                        .with(user(technicianUserDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Comunidade não assistida pelo técnico logado."));
    }

    @Test
    @DisplayName("GET /technicians/me/summary: deve retornar HTTP 404 quando cropId informado não existir")
    void deveRetornarHttp404QuandoCulturaNaoEncontrada() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        UUID nonExistentCropId = UUID.randomUUID();
        when(getTechnicianDashboardSummaryUseCase.execute(null, null, nonExistentCropId, technicianUser))
                .thenThrow(new CropNotFoundException("Cultura não encontrada."));

        mockMvc.perform(get("/analytics/technicians/me/summary")
                        .param("cropId", nonExistentCropId.toString())
                        .with(user(technicianUserDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cultura não encontrada."));
    }

    @Test
    @DisplayName("GET /technicians/me/summary: deve retornar HTTP 403 quando usuário logado for FARMER")
    void deveRetornarHttp403QuandoUsuarioForFarmer() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        mockMvc.perform(get("/analytics/technicians/me/summary")
                        .with(user(farmerUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /technicians/me/summary: deve rejeitar requisição não autenticada com HTTP 401")
    void deveRejeitarRequisicaoNaoAutenticadaParaTecnico() throws Exception {
        mockMvc.perform(get("/analytics/technicians/me/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ==========================================
    // Testes do Dashboard Macro-Gerencial (Feature 6.3)
    // ==========================================

    @Test
    @DisplayName("GET /organizations/me/summary: deve retornar HTTP 200 com resumo da organização do gestor")
    void deveRetornarResumoOrganizacaoDoGestor() throws Exception {
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        UserDetailsImpl managerUserDetails = userDetails(managerUser);
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        UUID orgId = UUID.randomUUID();
        ManagementDashboardSummaryDTO summary = ManagementDashboardSummaryDTO.empty(
                com.ufal.smartagro.domain.model.enums.ManagementDashboardScope.ORGANIZATION,
                orgId,
                "COOPAFAL",
                null,
                null
        );
        when(getManagementDashboardSummaryUseCase.executeForCurrentManager(null, null, managerUser))
                .thenReturn(summary);

        mockMvc.perform(get("/analytics/organizations/me/summary")
                        .with(user(managerUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("ORGANIZATION"))
                .andExpect(jsonPath("$.organizationId").value(orgId.toString()))
                .andExpect(jsonPath("$.organizationName").value("COOPAFAL"))
                .andExpect(jsonPath("$.isEmpty").value(true));
    }

    @Test
    @DisplayName("GET /organizations/me/summary: deve retornar HTTP 403 quando usuário logado for TECHNICIAN")
    void deveRetornarHttp403ParaTecnicoTentandoAcessarDashboardGerencial() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        mockMvc.perform(get("/analytics/organizations/me/summary")
                        .with(user(technicianUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /organizations/{organizationId}/summary: gestor deve conseguir consultar sua própria organização")
    void gestorDeveConsultarSuaPropriaOrganizacao() throws Exception {
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        UserDetailsImpl managerUserDetails = userDetails(managerUser);
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        UUID orgId = UUID.randomUUID();
        ManagementDashboardSummaryDTO summary = ManagementDashboardSummaryDTO.empty(
                com.ufal.smartagro.domain.model.enums.ManagementDashboardScope.ORGANIZATION,
                orgId,
                "COOPAFAL",
                null,
                null
        );
        when(getManagementDashboardSummaryUseCase.executeForOrganization(orgId, null, null, managerUser))
                .thenReturn(summary);

        mockMvc.perform(get("/analytics/organizations/{organizationId}/summary", orgId)
                        .with(user(managerUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(orgId.toString()));
    }

    @Test
    @DisplayName("GET /organizations/{organizationId}/summary: gestor tentando acessar outra organização deve tomar HTTP 403")
    void gestorTentandoAcessarOutraOrganizacaoDeveTomarHttp403() throws Exception {
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        UserDetailsImpl managerUserDetails = userDetails(managerUser);
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        UUID otherOrgId = UUID.randomUUID();
        when(getManagementDashboardSummaryUseCase.executeForOrganization(otherOrgId, null, null, managerUser))
                .thenThrow(new AccessDeniedException("Acesso negado: gestor não tem permissão para visualizar dados de outra organização."));

        mockMvc.perform(get("/analytics/organizations/{organizationId}/summary", otherOrgId)
                        .with(user(managerUserDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acesso negado: gestor não tem permissão para visualizar dados de outra organização."));
    }

    @Test
    @DisplayName("GET /organizations/{organizationId}/summary: deve retornar HTTP 404 quando organização não existir")
    void deveRetornarHttp404QuandoOrganizacaoNaoExistir() throws Exception {
        UUID nonExistentOrgId = UUID.randomUUID();
        when(getManagementDashboardSummaryUseCase.executeForOrganization(nonExistentOrgId, null, null, adminUser))
                .thenThrow(new EntityNotFoundException("Organização não encontrada."));

        mockMvc.perform(get("/analytics/organizations/{organizationId}/summary", nonExistentOrgId)
                        .with(user(adminUserDetails)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Organização não encontrada."));
    }

    @Test
    @DisplayName("GET /global/summary: admin deve conseguir acessar o dashboard global")
    void adminDeveAcessarDashboardGlobal() throws Exception {
        ManagementDashboardSummaryDTO summary = ManagementDashboardSummaryDTO.empty(
                com.ufal.smartagro.domain.model.enums.ManagementDashboardScope.GLOBAL,
                null,
                null,
                null,
                null
        );
        when(getManagementDashboardSummaryUseCase.executeGlobal(null, null, null, adminUser))
                .thenReturn(summary);

        mockMvc.perform(get("/analytics/global/summary")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("GLOBAL"))
                .andExpect(jsonPath("$.isEmpty").value(true));
    }

    @Test
    @DisplayName("GET /global/summary: gestor deve tomar HTTP 403 ao tentar acessar dashboard global")
    void gestorTentandoAcessarGlobalSummaryDeveTomarHttp403() throws Exception {
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        UserDetailsImpl managerUserDetails = userDetails(managerUser);
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        mockMvc.perform(get("/analytics/global/summary")
                        .with(user(managerUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /global/summary: técnico deve tomar HTTP 403 ao tentar acessar dashboard global")
    void tecnicoTentandoAcessarGlobalSummaryDeveTomarHttp403() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        mockMvc.perform(get("/analytics/global/summary")
                        .with(user(technicianUserDetails)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Testes de Exportação de Relatórios (Feature 6.5)
    // =========================================================================

    @Test
    @DisplayName("GET /farmers/me/summary/export: deve retornar 200 com binário PDF e registrar auditoria")
    void deveExportarResumoAgricultorEmPdf() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        FarmerDashboardSummaryDTO summary = FarmerDashboardSummaryDTO.empty(
                farmerUser.getId(), "João Agricultor", "Comunidade Sol Nascente", null
        );
        when(getFarmerDashboardSummaryUseCase.execute(null, farmerUser)).thenReturn(summary);
        byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46, 0x01, 0x02};
        when(reportExportService.exportFarmerSummaryToPdf(summary)).thenReturn(fakePdf);

        mockMvc.perform(get("/analytics/farmers/me/summary/export?format=pdf")
                        .with(user(farmerUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("relatorio-agricultor-")))
                .andExpect(content().bytes(fakePdf));

        verify(auditLogService).record(eq(farmerUser), eq(AuditAction.EXPORT), eq("analytics_reports"), any(), any());
    }

    @Test
    @DisplayName("GET /farmers/me/summary/export: deve retornar 200 com planilha XLSX")
    void deveExportarResumoAgricultorEmXlsx() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        FarmerDashboardSummaryDTO summary = FarmerDashboardSummaryDTO.empty(
                farmerUser.getId(), "João Agricultor", "Comunidade Sol Nascente", null
        );
        when(getFarmerDashboardSummaryUseCase.execute(null, farmerUser)).thenReturn(summary);
        byte[] fakeXlsx = new byte[]{0x50, 0x4B, 0x03, 0x04};
        when(reportExportService.exportFarmerSummaryToXlsx(summary)).thenReturn(fakeXlsx);

        mockMvc.perform(get("/analytics/farmers/me/summary/export?format=xlsx")
                        .with(user(farmerUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString(".xlsx")))
                .andExpect(content().bytes(fakeXlsx));
    }

    @Test
    @DisplayName("GET /farmers/me/summary/export: deve retornar 400 para formato inválido")
    void deveRetornar400ParaFormatoInvalido() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        mockMvc.perform(get("/analytics/farmers/me/summary/export?format=csv")
                        .with(user(farmerUserDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /farmers/me/summary/export: técnico deve tomar 403 ao tentar exportar relatório de agricultor")
    void tecnicoNaoPodeExportarRelatorioDeAgricultor() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        mockMvc.perform(get("/analytics/farmers/me/summary/export?format=pdf")
                        .with(user(technicianUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /technicians/me/summary/export: deve retornar 200 com PDF da carteira técnica")
    void deveExportarCarteiraTecnicaEmPdf() throws Exception {
        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        TechnicianDashboardSummaryDTO summary = TechnicianDashboardSummaryDTO.empty(
                technicianUser.getId(), "Carlos Técnico", null, null, null
        );
        when(getTechnicianDashboardSummaryUseCase.execute(null, null, null, technicianUser)).thenReturn(summary);
        byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46};
        when(reportExportService.exportTechnicianSummaryToPdf(summary)).thenReturn(fakePdf);

        mockMvc.perform(get("/analytics/technicians/me/summary/export?format=pdf")
                        .with(user(technicianUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("relatorio-carteira-tecnica-")))
                .andExpect(content().bytes(fakePdf));

        verify(auditLogService).record(eq(technicianUser), eq(AuditAction.EXPORT), eq("analytics_reports"), any(), any());
    }

    @Test
    @DisplayName("GET /technicians/me/summary/export: agricultor deve tomar 403 ao tentar exportar carteira técnica")
    void agricultorNaoPodeExportarCarteiraTecnica() throws Exception {
        User farmerUser = UserTestFactory.user().role(Role.FARMER).build();
        UserDetailsImpl farmerUserDetails = userDetails(farmerUser);
        when(userRepository.findById(farmerUser.getId())).thenReturn(Optional.of(farmerUser));

        mockMvc.perform(get("/analytics/technicians/me/summary/export?format=pdf")
                        .with(user(farmerUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /organizations/me/summary/export: gestor deve exportar resumo da organização em XLSX")
    void gestorDeveExportarOrganizacaoEmXlsx() throws Exception {
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        UserDetailsImpl managerUserDetails = userDetails(managerUser);
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        UUID orgId = UUID.randomUUID();
        ManagementDashboardSummaryDTO summary = ManagementDashboardSummaryDTO.empty(
                com.ufal.smartagro.domain.model.enums.ManagementDashboardScope.ORGANIZATION,
                orgId, "Cooperativa Alagoana", null, null
        );
        when(getManagementDashboardSummaryUseCase.executeForCurrentManager(null, null, managerUser)).thenReturn(summary);
        byte[] fakeXlsx = new byte[]{0x50, 0x4B, 0x03, 0x04};
        when(reportExportService.exportManagementSummaryToXlsx(summary)).thenReturn(fakeXlsx);

        mockMvc.perform(get("/analytics/organizations/me/summary/export?format=xlsx")
                        .with(user(managerUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("relatorio-organizacao-")))
                .andExpect(content().bytes(fakeXlsx));

        verify(auditLogService).record(eq(managerUser), eq(AuditAction.EXPORT), eq("analytics_reports"), eq(orgId), any());
    }

    @Test
    @DisplayName("GET /organizations/{organizationId}/summary/export: admin pode exportar qualquer organização")
    void adminPodeExportarOrganizacaoEspecifica() throws Exception {
        UUID orgId = UUID.randomUUID();
        ManagementDashboardSummaryDTO summary = ManagementDashboardSummaryDTO.empty(
                com.ufal.smartagro.domain.model.enums.ManagementDashboardScope.ORGANIZATION,
                orgId, "Cooperativa Central", null, null
        );
        when(getManagementDashboardSummaryUseCase.executeForOrganization(eq(orgId), any(), any(), eq(adminUser)))
                .thenReturn(summary);
        byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46};
        when(reportExportService.exportManagementSummaryToPdf(summary)).thenReturn(fakePdf);

        mockMvc.perform(get("/analytics/organizations/" + orgId + "/summary/export?format=pdf")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(content().bytes(fakePdf));
    }

    @Test
    @DisplayName("GET /global/summary/export: admin exporta dashboard global em PDF")
    void adminPodeExportarDashboardGlobal() throws Exception {
        ManagementDashboardSummaryDTO summary = ManagementDashboardSummaryDTO.empty(
                com.ufal.smartagro.domain.model.enums.ManagementDashboardScope.GLOBAL,
                null, null, null, null
        );
        when(getManagementDashboardSummaryUseCase.executeGlobal(null, null, null, adminUser))
                .thenReturn(summary);
        byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46};
        when(reportExportService.exportManagementSummaryToPdf(summary)).thenReturn(fakePdf);

        mockMvc.perform(get("/analytics/global/summary/export?format=pdf")
                        .with(user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("relatorio-gerencial-global.pdf")))
                .andExpect(content().bytes(fakePdf));

        verify(auditLogService).record(eq(adminUser), eq(AuditAction.EXPORT), eq("analytics_reports"), any(), any());
    }

    @Test
    @DisplayName("GET /global/summary/export: gestor e técnico tomam 403 ao tentar exportar global")
    void gestorETecnicoNaoPodemExportarGlobal() throws Exception {
        User managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        UserDetailsImpl managerUserDetails = userDetails(managerUser);
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        mockMvc.perform(get("/analytics/global/summary/export?format=pdf")
                        .with(user(managerUserDetails)))
                .andExpect(status().isForbidden());

        User technicianUser = UserTestFactory.user().role(Role.TECHNICIAN).build();
        UserDetailsImpl technicianUserDetails = userDetails(technicianUser);
        when(userRepository.findById(technicianUser.getId())).thenReturn(Optional.of(technicianUser));

        mockMvc.perform(get("/analytics/global/summary/export?format=pdf")
                        .with(user(technicianUserDetails)))
                .andExpect(status().isForbidden());
    }
}
