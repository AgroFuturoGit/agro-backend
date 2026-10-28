package com.ufal.smartagro.adapters.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestRegisterDTO;
import com.ufal.smartagro.application.service.harvest.*;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HarvestController.class)
@Import(SecurityConfig.class)
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
    void registerHarvest_legacyRegisterPost_returnsCreatedAndLocationHeader() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID harvestId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        Harvest savedHarvest = new Harvest(harvestId, "Safra 2026.2", start, end);

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(harvestRegisterUseCase.execute(any(), any())).thenReturn(savedHarvest);

        HarvestRegisterDTO dto = new HarvestRegisterDTO("Safra 2026.2", start, end);

        mockMvc.perform(post("/harvests/register")
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/harvests/" + harvestId))
                .andExpect(jsonPath("$.id").value(harvestId.toString()));
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
