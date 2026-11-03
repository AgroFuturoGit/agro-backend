package com.ufal.smartagro.adapters.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationUpdateDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.community.RegisterCommunityUseCase;
import com.ufal.smartagro.application.service.manager.RegisterManagerUseCase;
import com.ufal.smartagro.application.service.organization.DeleteOrganizationUseCase;
import com.ufal.smartagro.application.service.organization.FindAllOrganizationsUseCase;
import com.ufal.smartagro.application.service.organization.FindOrganizationByIdUseCase;
import com.ufal.smartagro.application.service.organization.RegisterOrganizationUseCase;
import com.ufal.smartagro.application.service.organization.UpdateOrganizationUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.OrganizationType;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
@Import(SecurityConfig.class)
@DisplayName("OrganizationController — validações de endpoints e RBAC")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterOrganizationUseCase registerOrganizationUseCase;

    @MockitoBean
    private RegisterManagerUseCase registerManagerUseCase;

    @MockitoBean
    private RegisterCommunityUseCase registerCommunityUseCase;

    @MockitoBean
    private FindOrganizationByIdUseCase findOrganizationByIdUseCase;

    @MockitoBean
    private FindAllOrganizationsUseCase findAllOrganizationsUseCase;

    @MockitoBean
    private UpdateOrganizationUseCase updateOrganizationUseCase;

    @MockitoBean
    private DeleteOrganizationUseCase deleteOrganizationUseCase;

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
    @DisplayName("POST /organizations — ADMIN pode cadastrar organização (201)")
    void registerOrganization_asAdmin_returnsCreated() throws Exception {
        User admin = UserTestFactory.user().admin().build();
        OrganizationRegisterDTO dto = new OrganizationRegisterDTO("Coop Norte", "12345678000199", OrganizationType.COOP);
        Organization saved = new Organization(UUID.randomUUID(), "Coop Norte", "12345678000199", OrganizationType.COOP, null, null, null);

        when(registerOrganizationUseCase.register(any(), any())).thenReturn(saved);

        mockMvc.perform(post("/organizations")
                        .with(user(userDetails(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()))
                .andExpect(jsonPath("$.name").value("Coop Norte"));
    }

    @Test
    @DisplayName("POST /organizations — não-ADMIN recebe 403 Forbidden")
    void registerOrganization_asManager_returnsForbidden() throws Exception {
        User manager = UserTestFactory.user().manager().build();
        OrganizationRegisterDTO dto = new OrganizationRegisterDTO("Coop Norte", "12345678000199", OrganizationType.COOP);

        mockMvc.perform(post("/organizations")
                        .with(user(userDetails(manager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /organizations — ADMIN pode listar (200)")
    void findAllOrganizations_asAdmin_returnsOk() throws Exception {
        User admin = UserTestFactory.user().admin().build();
        Organization org = new Organization(UUID.randomUUID(), "Coop Norte", "12345678000199", OrganizationType.COOP, null, null, null);

        when(findAllOrganizationsUseCase.findAll(any())).thenReturn(List.of(org));

        mockMvc.perform(get("/organizations")
                        .with(user(userDetails(admin)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Coop Norte"));
    }

    @Test
    @DisplayName("GET /organizations — MANAGER recebe 403 Forbidden")
    void findAllOrganizations_asManager_returnsForbidden() throws Exception {
        User manager = UserTestFactory.user().manager().build();

        mockMvc.perform(get("/organizations")
                        .with(user(userDetails(manager)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /organizations/{id} — MANAGER pode consultar (200)")
    void findOrganizationById_asManager_returnsOk() throws Exception {
        UUID orgId = UUID.randomUUID();
        User manager = UserTestFactory.user().manager().build();
        Organization org = new Organization(orgId, "Coop Norte", "12345678000199", OrganizationType.COOP, null, null, null);

        when(findOrganizationByIdUseCase.findById(eq(orgId), any())).thenReturn(org);

        mockMvc.perform(get("/organizations/{id}", orgId)
                        .with(user(userDetails(manager)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value("Coop Norte"));
    }

    @Test
    @DisplayName("GET /organizations/{id} — FARMER recebe 403 Forbidden")
    void findOrganizationById_asFarmer_returnsForbidden() throws Exception {
        UUID orgId = UUID.randomUUID();
        User farmer = UserTestFactory.user().farmer().build();

        mockMvc.perform(get("/organizations/{id}", orgId)
                        .with(user(userDetails(farmer)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /organizations/{id} — ADMIN pode atualizar (200)")
    void updateOrganization_asAdmin_returnsOk() throws Exception {
        UUID orgId = UUID.randomUUID();
        User admin = UserTestFactory.user().admin().build();
        OrganizationUpdateDTO dto = new OrganizationUpdateDTO("Coop Atualizada", OrganizationType.ASSOC);
        Organization updated = new Organization(orgId, "Coop Atualizada", "12345678000199", OrganizationType.ASSOC, null, null, null);

        when(updateOrganizationUseCase.update(eq(orgId), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/organizations/{id}", orgId)
                        .with(user(userDetails(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Coop Atualizada"));
    }

    @Test
    @DisplayName("PUT /organizations/{id} — MANAGER recebe 403 Forbidden")
    void updateOrganization_asManager_returnsForbidden() throws Exception {
        UUID orgId = UUID.randomUUID();
        User manager = UserTestFactory.user().manager().build();
        OrganizationUpdateDTO dto = new OrganizationUpdateDTO("Coop Atualizada", OrganizationType.ASSOC);

        mockMvc.perform(put("/organizations/{id}", orgId)
                        .with(user(userDetails(manager)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /organizations/{id} — ADMIN pode excluir (204)")
    void deleteOrganization_asAdmin_returnsNoContent() throws Exception {
        UUID orgId = UUID.randomUUID();
        User admin = UserTestFactory.user().admin().build();

        mockMvc.perform(delete("/organizations/{id}", orgId)
                        .with(user(userDetails(admin))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /organizations/{id} — MANAGER recebe 403 Forbidden")
    void deleteOrganization_asManager_returnsForbidden() throws Exception {
        UUID orgId = UUID.randomUUID();
        User manager = UserTestFactory.user().manager().build();

        mockMvc.perform(delete("/organizations/{id}", orgId)
                        .with(user(userDetails(manager))))
                .andExpect(status().isForbidden());
    }
}
