package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.analytics.HarvestTimelineDTO;
import com.ufal.smartagro.adapters.in.web.dto.analytics.TimelinePointDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.production.GetHarvestTimelineUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.HarvestType;
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
}
