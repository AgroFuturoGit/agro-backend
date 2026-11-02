package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.application.service.community.DeleteCommunityUseCase;
import com.ufal.smartagro.application.service.community.FindAllCommunitiesUseCase;
import com.ufal.smartagro.application.service.community.FindCommunityByIdUseCase;
import com.ufal.smartagro.application.service.community.UpdateCommunityUseCase;
import com.ufal.smartagro.application.service.farmer.RegisterFarmerUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.port.out.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommunityController.class)
@Import(SecurityConfig.class)
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterFarmerUseCase registerFarmerUseCase;

    @MockitoBean
    private FindCommunityByIdUseCase findCommunityByIdUseCase;

    @MockitoBean
    private FindAllCommunitiesUseCase findAllCommunitiesUseCase;

    @MockitoBean
    private UpdateCommunityUseCase updateCommunityUseCase;

    @MockitoBean
    private DeleteCommunityUseCase deleteCommunityUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void makeJwtFilterContinueTheSecurityChain() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void returnsCommunitiesForManager() throws Exception {
        UUID id = UUID.randomUUID();
        when(findAllCommunitiesUseCase.findAll(null)).thenReturn(List.of(
                new Community(id, "Comunidade Norte", null, null, null, null)));

        mockMvc.perform(get("/communities")
                        .with(user("manager").roles("MANAGER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Comunidade Norte"))
                .andExpect(jsonPath("$[0].organization").doesNotExist());
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/communities"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void returnsCommunitiesForFarmer() throws Exception {
        UUID id = UUID.randomUUID();
        when(findAllCommunitiesUseCase.findAll(null)).thenReturn(List.of(
                new Community(id, "Comunidade Sul", null, null, null, null)));

        mockMvc.perform(get("/communities")
                        .with(user("farmer").roles("FARMER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Comunidade Sul"));
    }

    @Test
    void returnsCommunitiesForTechnician() throws Exception {
        UUID id = UUID.randomUUID();
        when(findAllCommunitiesUseCase.findAll(null)).thenReturn(List.of(
                new Community(id, "Comunidade Leste", null, null, null, null)));

        mockMvc.perform(get("/communities")
                        .with(user("technician").roles("TECHNICIAN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Comunidade Leste"));
    }

    @Test
    void registerFarmer_asTechnician_returnsCreated() throws Exception {
        UUID communityId = UUID.randomUUID();
        com.ufal.smartagro.domain.model.Farmer farmer = com.ufal.smartagro.testsupport.UserTestFactory.farmerProfile().build();
        com.ufal.smartagro.domain.model.User techUser = com.ufal.smartagro.testsupport.UserTestFactory.user().technician().build();

        when(userRepository.findById(any())).thenReturn(java.util.Optional.of(techUser));
        when(registerFarmerUseCase.register(any(), any(), any())).thenReturn(farmer);

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO dto = com.ufal.smartagro.testsupport.UserTestFactory.farmerRegisterDto().build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/communities/{id}/farmers", communityId)
                        .with(user(userDetails(techUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(farmer.getId().toString()));
    }

    private com.ufal.smartagro.config.security.details.UserDetailsImpl userDetails(com.ufal.smartagro.domain.model.User user) {
        com.ufal.smartagro.adapters.out.persistence.entity.UserEntity entity = new com.ufal.smartagro.adapters.out.persistence.entity.UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setRole(user.getRole());
        return new com.ufal.smartagro.config.security.details.UserDetailsImpl(entity);
    }
}
