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
                .andExpect(status().isForbidden());
    }
}
