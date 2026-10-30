package com.ufal.smartagro.config.security.filter;

import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.service.JwtTokenService;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("API-06: deve ignorar token inválido no header quando a rota for /auth/login")
    void shouldIgnoreInvalidTokenOnLoginRoute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        request.setServletPath("/auth/login");
        request.addHeader("Authorization", "Bearer garbage-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verifyNoInteractions(jwtTokenService, userDetailsService);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("API-05: token inválido em rota protegida não deve lançar 500 e deve limpar o SecurityContext")
    void shouldNotThrowExceptionOnInvalidTokenAndClearSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        request.setServletPath("/auth/me");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenService.getSubjectFromToken("invalid-token"))
                .thenThrow(new RuntimeException("Token inválido ou expirado."));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Deve autenticar normalmente quando o token JWT for válido")
    void shouldAuthenticateValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        request.setServletPath("/auth/me");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetailsImpl userDetails = new UserDetailsImpl(
                UserTestFactory.user().admin().email("admin@smartagro.test").toEntity()
        );

        when(jwtTokenService.getSubjectFromToken("valid-token")).thenReturn("admin@smartagro.test");
        when(userDetailsService.loadUserByUsername("admin@smartagro.test")).thenReturn(userDetails);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
    }
}
