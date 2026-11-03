package com.ufal.smartagro.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerUpdateDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.farmer.DeleteFarmerUseCase;
import com.ufal.smartagro.application.service.farmer.FindAllFarmersUseCase;
import com.ufal.smartagro.application.service.farmer.FindFarmerByIdUseCase;
import com.ufal.smartagro.application.service.farmer.FindFarmerByUserUseCase;
import com.ufal.smartagro.application.service.farmer.UpdateFarmerUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
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

@WebMvcTest(FarmerController.class)
@Import(SecurityConfig.class)
@DisplayName("FarmerController — validações de endpoints e RBAC")
class FarmerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindFarmerByUserUseCase findFarmerByUserUseCase;

    @MockitoBean
    private FindFarmerByIdUseCase findFarmerByIdUseCase;

    @MockitoBean
    private FindAllFarmersUseCase findAllFarmersUseCase;

    @MockitoBean
    private UpdateFarmerUseCase updateFarmerUseCase;

    @MockitoBean
    private DeleteFarmerUseCase deleteFarmerUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    @DisplayName("GET /farmers/me — FARMER autenticado acessa seus dados com sucesso")
    void givenFarmer_whenFindMyFarmerData_thenReturnsOk() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        Farmer farmer = UserTestFactory.farmerProfile().user(farmerUser).build();
        when(findFarmerByUserUseCase.findByUserId(farmerUser.getId())).thenReturn(farmer);

        mockMvc.perform(get("/farmers/me")
                        .with(user(userDetailsFor(farmerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(farmer.getId().toString()))
                .andExpect(jsonPath("$.fullName").value(farmer.getFullName()));
    }

    @Test
    @DisplayName("GET /farmers/me — TECHNICIAN não tem permissão (403 Forbidden)")
    void givenTechnician_whenFindMyFarmerData_thenForbidden() throws Exception {
        User techUser = UserTestFactory.user().technician().build();

        mockMvc.perform(get("/farmers/me")
                        .with(user(userDetailsFor(techUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /farmers — ADMIN, MANAGER e TECHNICIAN podem listar agricultores")
    void givenManager_whenFindAllFarmers_thenReturnsOk() throws Exception {
        User manager = UserTestFactory.user().manager().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        when(findAllFarmersUseCase.findAll(any(), any())).thenReturn(List.of(farmer));

        mockMvc.perform(get("/farmers")
                        .with(user(userDetailsFor(manager))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(farmer.getId().toString()));
    }

    @Test
    @DisplayName("GET /farmers — FARMER não pode listar outros agricultores (403 Forbidden)")
    void givenFarmer_whenFindAllFarmers_thenForbidden() throws Exception {
        User farmer = UserTestFactory.user().farmer().build();

        mockMvc.perform(get("/farmers")
                        .with(user(userDetailsFor(farmer))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /farmers/{id} — Usuário autorizado obtém detalhes do produtor")
    void givenAuthorizedUser_whenFindById_thenReturnsOk() throws Exception {
        User admin = UserTestFactory.user().admin().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        when(findFarmerByIdUseCase.findById(eq(farmer.getId()), any())).thenReturn(farmer);

        mockMvc.perform(get("/farmers/{id}", farmer.getId())
                        .with(user(userDetailsFor(admin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(farmer.getId().toString()));
    }

    @Test
    @DisplayName("PUT /farmers/{id} — Atualiza produtor com sucesso")
    void givenManager_whenUpdateFarmer_thenReturnsOk() throws Exception {
        User manager = UserTestFactory.user().manager().build();
        Farmer farmer = UserTestFactory.farmerProfile().build();
        FarmerUpdateDTO dto = new FarmerUpdateDTO("Apelido Novo");
        when(updateFarmerUseCase.update(eq(farmer.getId()), any(), any())).thenReturn(farmer);

        mockMvc.perform(put("/farmers/{id}", farmer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(user(userDetailsFor(manager))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /farmers/{id} — MANAGER ou ADMIN exclui com sucesso (204 No Content)")
    void givenManager_whenDeleteFarmer_thenReturnsNoContent() throws Exception {
        User manager = UserTestFactory.user().manager().build();
        UUID farmerId = UUID.randomUUID();

        mockMvc.perform(delete("/farmers/{id}", farmerId)
                        .with(user(userDetailsFor(manager))))
                .andExpect(status().isNoContent());

        verify(deleteFarmerUseCase).delete(eq(farmerId), any());
    }

    @Test
    @DisplayName("DELETE /farmers/{id} — TECHNICIAN não tem permissão para excluir agricultores (403 Forbidden)")
    void givenTechnician_whenDeleteFarmer_thenForbidden() throws Exception {
        User tech = UserTestFactory.user().technician().build();
        UUID farmerId = UUID.randomUUID();

        mockMvc.perform(delete("/farmers/{id}", farmerId)
                        .with(user(userDetailsFor(tech))))
                .andExpect(status().isForbidden());
    }
}
