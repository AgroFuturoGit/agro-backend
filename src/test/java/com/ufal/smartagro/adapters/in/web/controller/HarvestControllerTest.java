package com.ufal.smartagro.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestUpdateDTO;
import com.ufal.smartagro.application.service.harvest.HarvestDeleteUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestFindAllUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestFindByIdUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestRegisterUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestUpdateUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

@WebMvcTest(HarvestController.class)
@Import(SecurityConfig.class)
@DisplayName("HarvestController — validações de endpoints e RBAC")
class HarvestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HarvestRegisterUseCase harvestRegisterUseCase;

    @MockitoBean
    private HarvestUpdateUseCase harvestUpdateUseCase;

    @MockitoBean
    private HarvestFindByIdUseCase harvestFindByIdUseCase;

    @MockitoBean
    private HarvestFindAllUseCase harvestFindAllUseCase;

    @MockitoBean
    private HarvestDeleteUseCase harvestDeleteUseCase;

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

    @Test
    @DisplayName("POST /harvests — ADMIN registra safra com sucesso (201 Created)")
    void registerHarvest_standardPost_returnsCreatedAndLocationHeader() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        Harvest savedHarvest = new Harvest(harvestId, "Safra 2026.1", start, end);

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(harvestRegisterUseCase.execute(any(), any())).thenReturn(savedHarvest);

        HarvestRegisterDTO dto = new HarvestRegisterDTO("Safra 2026.1", start, end);

        mockMvc.perform(post("/harvests")
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/harvests/" + harvestId))
                .andExpect(jsonPath("$.id").value(harvestId.toString()))
                .andExpect(jsonPath("$.label").value("Safra 2026.1"));
    }

    @Test
    @DisplayName("POST /harvests — FARMER não pode registrar safra (403 Forbidden)")
    void registerHarvest_asFarmer_returnsForbidden() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        HarvestRegisterDTO dto = new HarvestRegisterDTO("Safra 2026.1", start, end);

        mockMvc.perform(post("/harvests")
                        .with(user(userDetails(farmerUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /harvests/{id} — TECHNICIAN pode atualizar safra (200 OK)")
    void updateHarvest_asTechnician_returnsOk() throws Exception {
        User techUser = UserTestFactory.user().technician().build();
        UUID harvestId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        Harvest updatedHarvest = new Harvest(harvestId, "Safra 2026.1 - Atualizada", start, end);
        when(harvestUpdateUseCase.execute(eq(harvestId), any(), any(), any())).thenReturn(updatedHarvest);

        HarvestUpdateDTO dto = new HarvestUpdateDTO("Safra 2026.1 - Atualizada", start, end);

        mockMvc.perform(patch("/harvests/{id}", harvestId)
                        .with(user(userDetails(techUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Safra 2026.1 - Atualizada"));
    }

    @Test
    @DisplayName("PUT /harvests/{id} — ADMIN pode atualizar safra (200 OK)")
    void updateHarvest_asAdmin_returnsOk() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);
        Harvest updatedHarvest = new Harvest(harvestId, "Safra 2026.1", start, end);
        when(harvestUpdateUseCase.execute(eq(harvestId), any(), any(), any())).thenReturn(updatedHarvest);

        HarvestUpdateDTO dto = new HarvestUpdateDTO("Safra 2026.1", start, end);

        mockMvc.perform(put("/harvests/{id}", harvestId)
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /harvests — FARMER pode consultar safras (200 OK)")
    void findAllHarvests_asFarmer_returnsOk() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        Harvest harvest = new Harvest(UUID.randomUUID(), "Safra 2026.1", LocalDate.now(), LocalDate.now().plusMonths(6));
        when(harvestFindAllUseCase.execute(any())).thenReturn(List.of(harvest));

        mockMvc.perform(get("/harvests")
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Safra 2026.1"));
    }

    @Test
    @DisplayName("GET /harvests/{id} — FARMER pode consultar detalhes de safra (200 OK)")
    void findHarvestById_asFarmer_returnsOk() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        UUID harvestId = UUID.randomUUID();
        Harvest harvest = new Harvest(harvestId, "Safra 2026.1", LocalDate.now(), LocalDate.now().plusMonths(6));
        when(harvestFindByIdUseCase.execute(eq(harvestId), any())).thenReturn(harvest);

        mockMvc.perform(get("/harvests/{id}", harvestId)
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(harvestId.toString()));
    }

    @Test
    @DisplayName("DELETE /harvests/{id} — ADMIN pode excluir safra (204 No Content)")
    void deleteHarvest_asAdmin_returnsNoContent() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();

        mockMvc.perform(delete("/harvests/{id}", harvestId)
                        .with(user(userDetails(adminUser))))
                .andExpect(status().isNoContent());

        verify(harvestDeleteUseCase).execute(eq(harvestId), any());
    }

    @Test
    @DisplayName("DELETE /harvests/{id} — FARMER não pode excluir safra (403 Forbidden)")
    void deleteHarvest_asFarmer_returnsForbidden() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        UUID harvestId = UUID.randomUUID();

        mockMvc.perform(delete("/harvests/{id}", harvestId)
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isForbidden());
    }

    private UserDetailsImpl userDetails(User user) {
        var entity = new com.ufal.smartagro.adapters.out.persistence.entity.UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setRole(user.getRole());
        return new UserDetailsImpl(entity);
    }
}
