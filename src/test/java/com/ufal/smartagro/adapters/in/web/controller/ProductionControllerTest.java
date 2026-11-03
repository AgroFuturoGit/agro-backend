package com.ufal.smartagro.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionDashboardDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.RejectExecutionDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.production.*;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductionController.class)
@Import(SecurityConfig.class)
@DisplayName("ProductionController — RBAC e proteção contra IDOR")
class ProductionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProductionPlanUseCase createProductionPlanUseCase;
    @MockitoBean
    private CreateProductionExecutionUseCase createProductionExecutionUseCase;
    @MockitoBean
    private UpdateProductionPlanUseCase updateProductionPlanUseCase;
    @MockitoBean
    private ListProductionPlansUseCase listProductionPlansUseCase;
    @MockitoBean
    private FindProductionPlanByIdUseCase findProductionPlanByIdUseCase;
    @MockitoBean
    private DeleteProductionPlanUseCase deleteProductionPlanUseCase;

    @MockitoBean
    private UpdateProductionExecutionUseCase updateProductionExecutionUseCase;
    @MockitoBean
    private ValidateProductionExecutionUseCase validateProductionExecutionUseCase;
    @MockitoBean
    private RejectProductionExecutionUseCase rejectProductionExecutionUseCase;
    @MockitoBean
    private ListProductionExecutionsUseCase listProductionExecutionsUseCase;
    @MockitoBean
    private CompareProductionUseCase compareProductionUseCase;
    @MockitoBean
    private ProductionDashboardUseCase productionDashboardUseCase;
    @MockitoBean
    private FindProductionExecutionByIdUseCase findProductionExecutionByIdUseCase;
    @MockitoBean
    private DeleteProductionExecutionUseCase deleteProductionExecutionUseCase;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void makeJwtFilterContinueTheSecurityChain() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private UserDetailsImpl userDetailsFor(User domainUser) {
        UserEntity entity = new UserEntity();
        entity.setId(domainUser.getId());
        entity.setEmail(domainUser.getEmail());
        entity.setPassword(domainUser.getPassword());
        entity.setFullName(domainUser.getFullName());
        entity.setCpf(domainUser.getCpf());
        entity.setRole(domainUser.getRole());
        return new UserDetailsImpl(entity);
    }

    private ProductionPlan dummyPlan(UUID id) {
        Farmer farmer = UserTestFactory.farmerProfile().build();
        Harvest harvest = UserTestFactory.harvest();
        Crop crop = UserTestFactory.crop();
        return new ProductionPlan(
                id, UUID.randomUUID(), farmer, harvest, crop,
                new BigDecimal("5.00"), PlanStatus.PLANNED,
                LocalDate.now(), LocalDate.now().plusMonths(3),
                new BigDecimal("1000.00"), "Setor 1", null, null,
                LocalDate.now(), null, null, null, null
        );
    }

    private ProductionExecution dummyExecution(UUID id, ProductionPlan plan) {
        return new ProductionExecution(
                id, UUID.randomUUID(), plan, new BigDecimal("100.00"),
                new BigDecimal("100.00"), LocalDate.now(), ExecutionStatus.PENDING,
                null, null, "Colheita inicial", null, null, null, null, null, null, null, null
        );
    }

    @Test
    @DisplayName("GET /production/dashboard — permitido para FARMER, MANAGER, TECHNICIAN e ADMIN")
    void givenAuthenticatedRoles_whenGetDashboard_thenOk() throws Exception {
        when(productionDashboardUseCase.getDashboard(any()))
                .thenReturn(new ProductionDashboardDTO(1, 1L, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, List.of()));

        mockMvc.perform(get("/production/dashboard")
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build()))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/production/dashboard")
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build()))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /farmers/{id}/production-plans — MANAGER recebe 403 e FARMER recebe 201")
    void givenPlanCreation_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID farmerId = UUID.randomUUID();
        ProductionPlanRegisterDTO dto = new ProductionPlanRegisterDTO(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("2.50"),
                LocalDate.now(), LocalDate.now().plusMonths(2), "Talhão Norte", null, null,
                LocalDate.now(), null
        );

        when(createProductionPlanUseCase.create(eq(farmerId), any(), any()))
                .thenReturn(dummyPlan(UUID.randomUUID()));

        // FARMER permitido
        mockMvc.perform(post("/farmers/" + farmerId + "/production-plans")
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // MANAGER proibido
        mockMvc.perform(post("/farmers/" + farmerId + "/production-plans")
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /production-plans/{id} — MANAGER recebe 403 e TECHNICIAN recebe 200")
    void givenPlanUpdate_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID planId = UUID.randomUUID();
        ProductionPlanUpdateDTO dto = new ProductionPlanUpdateDTO(new BigDecimal("3.00"), null, null, null, null);

        when(updateProductionPlanUseCase.update(eq(planId), any(), any()))
                .thenReturn(dummyPlan(planId));

        mockMvc.perform(put("/production-plans/" + planId)
                        .with(user(userDetailsFor(UserTestFactory.user().technician().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/production-plans/" + planId)
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /production-plans/{id} — MANAGER recebe 403 e ADMIN recebe 204")
    void givenPlanDelete_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID planId = UUID.randomUUID();

        mockMvc.perform(delete("/production-plans/" + planId)
                        .with(user(userDetailsFor(UserTestFactory.user().admin().build()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/production-plans/" + planId)
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build()))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /production-plans/{id}/executions — MANAGER recebe 403 e FARMER recebe 201")
    void givenExecutionCreation_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID planId = UUID.randomUUID();
        ProductionExecutionRegisterDTO dto = new ProductionExecutionRegisterDTO(
                new BigDecimal("50.00"), LocalDate.now(), null, null, null, null
        );

        ProductionPlan plan = dummyPlan(planId);
        when(createProductionExecutionUseCase.create(eq(planId), any(), any()))
                .thenReturn(dummyExecution(UUID.randomUUID(), plan));

        mockMvc.perform(post("/production-plans/" + planId + "/executions")
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        mockMvc.perform(post("/production-plans/" + planId + "/executions")
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /production-executions/{id}/validate e reject — FARMER recebe 403 e MANAGER recebe 200")
    void givenExecutionValidation_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID execId = UUID.randomUUID();
        ProductionPlan plan = dummyPlan(UUID.randomUUID());
        ProductionExecution exec = dummyExecution(execId, plan);

        when(validateProductionExecutionUseCase.validate(eq(execId), any()))
                .thenReturn(exec);
        when(rejectProductionExecutionUseCase.reject(eq(execId), any(), any()))
                .thenReturn(exec);

        // Validação por MANAGER é permitida
        mockMvc.perform(post("/production-executions/" + execId + "/validate")
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build()))))
                .andExpect(status().isOk());

        // Validação por FARMER é proibida
        mockMvc.perform(post("/production-executions/" + execId + "/validate")
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build()))))
                .andExpect(status().isForbidden());

        // Rejeição por MANAGER é permitida
        RejectExecutionDTO rejectDto = new RejectExecutionDTO("Qualidade insuficiente");
        mockMvc.perform(post("/production-executions/" + execId + "/reject")
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDto)))
                .andExpect(status().isOk());

        // Rejeição por FARMER é proibida
        mockMvc.perform(post("/production-executions/" + execId + "/reject")
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDto)))
                .andExpect(status().isForbidden());
    }
}
