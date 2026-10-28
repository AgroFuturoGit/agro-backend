package com.ufal.smartagro.config.cors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    @Test
    @DisplayName("Deve configurar origens restritas e allowCredentials=false por padrão")
    void shouldConfigureStrictOriginsWithoutCredentials() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", List.of("http://localhost:3000", "http://localhost:5173"));
        ReflectionTestUtils.setField(corsConfig, "allowCredentials", false);

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/crops");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertEquals(Boolean.FALSE, config.getAllowCredentials());
        assertEquals(List.of("http://localhost:3000", "http://localhost:5173"), config.getAllowedOrigins());
    }

    @Test
    @DisplayName("Deve usar allowedOriginPatterns caso origem contenha wildcard")
    void shouldConfigurePatternsWhenWildcardPresent() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", List.of("https://*.smartagro.com"));
        ReflectionTestUtils.setField(corsConfig, "allowCredentials", true);

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/crops");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertNotNull(config);
        assertEquals(Boolean.TRUE, config.getAllowCredentials());
        assertEquals(List.of("https://*.smartagro.com"), config.getAllowedOriginPatterns());
    }
}
