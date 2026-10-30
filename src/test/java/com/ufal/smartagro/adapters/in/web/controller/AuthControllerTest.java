package com.ufal.smartagro.adapters.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufal.smartagro.adapters.in.web.dto.auth.LoginDTO;
import com.ufal.smartagro.adapters.in.web.exception.handler.GlobalExceptionHandler;
import com.ufal.smartagro.application.service.auth.LoginUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.config.security.ratelimit.LoginRateLimiter;
import com.ufal.smartagro.domain.exception.RateLimitExceededException;
import com.ufal.smartagro.domain.model.LoginResult;
import com.ufal.smartagro.domain.model.User;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private LoginRateLimiter rateLimiter;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Given credenciais válidas When /auth/login Then retorna 200 com token e dados do usuário")
    void givenValidCredentials_whenLogin_thenReturns200WithToken() throws Exception {
        LoginDTO loginDTO = new LoginDTO("admin@smartagro.test", "12345678");
        User user = UserTestFactory.user().admin().email("admin@smartagro.test").fullName("Admin Teste").build();
        when(loginUseCase.login(any(LoginDTO.class))).thenReturn(new LoginResult("fake-jwt-token", user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.userResponseDTO.email").value("admin@smartagro.test"))
                .andExpect(jsonPath("$.userResponseDTO.fullName").value("Admin Teste"));
    }

    @Test
    @DisplayName("Given excesso de tentativas When /auth/login Then retorna 429 com Retry-After header")
    void givenRateLimitExceeded_whenLogin_thenReturns429WithRetryAfterHeader() throws Exception {
        LoginDTO loginDTO = new LoginDTO("attacker@test.com", "wrong-password");
        doThrow(new RateLimitExceededException("Limite de tentativas de login excedido. Tente novamente em 45 segundos.", 45))
                .when(rateLimiter).checkAndIncrement(any());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "45"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Limite de tentativas de login excedido. Tente novamente em 45 segundos."));
    }

    @Test
    @DisplayName("Given usuário autenticado When /auth/me Then retorna 200 com dados do usuário")
    void givenAuthenticatedUser_whenGetMe_thenReturns200() throws Exception {
        User user = UserTestFactory.user().technician().email("tech@smartagro.test").fullName("Tecnico Teste").build();
        UserDetailsImpl userDetails = new UserDetailsImpl(user, "password123");

        mockMvc.perform(get("/auth/me").with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("tech@smartagro.test"))
                .andExpect(jsonPath("$.fullName").value("Tecnico Teste"));
    }

    @Test
    @DisplayName("Given usuário não autenticado When /auth/me Then retorna 401 com mensagem padronizada")
    void givenUnauthenticated_whenGetMe_thenReturns401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Não autenticado. Token ausente, inválido ou expirado."));
    }

    @Test
    @DisplayName("Given swagger desabilitado When acessa /swagger-ui/index.html Then retorna 401")
    void givenSwaggerDisabled_whenAccessingDocs_thenReturns401() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized());
    }
}
