package com.ufal.smartagro.config.security.ratelimit;

import com.ufal.smartagro.domain.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiter = new LoginRateLimiter();
        setField("enabled", true);
        setField("maxAttempts", 3);
        setField("windowSeconds", 60L);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = LoginRateLimiter.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(rateLimiter, value);
    }

    @Test
    @DisplayName("Given tentativas dentro do limite When checkAndIncrement Then não lança exceção")
    void givenAttemptsWithinLimit_whenCheckAndIncrement_thenPasses() {
        String clientKey = "127.0.0.1:user@smartagro.test";

        assertThatCode(() -> {
            rateLimiter.checkAndIncrement(clientKey);
            rateLimiter.checkAndIncrement(clientKey);
            rateLimiter.checkAndIncrement(clientKey);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Given tentativas excedendo o limite When checkAndIncrement Then lança RateLimitExceededException com retryAfter")
    void givenAttemptsExceedingLimit_whenCheckAndIncrement_thenThrowsRateLimitExceededException() {
        String clientKey = "127.0.0.1:user@smartagro.test";

        rateLimiter.checkAndIncrement(clientKey);
        rateLimiter.checkAndIncrement(clientKey);
        rateLimiter.checkAndIncrement(clientKey);

        assertThatThrownBy(() -> rateLimiter.checkAndIncrement(clientKey))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> {
                    RateLimitExceededException rle = (RateLimitExceededException) ex;
                    assertThat(rle.getRetryAfterSeconds()).isGreaterThan(0L).isLessThanOrEqualTo(60L);
                    assertThat(rle.getMessage()).contains("Limite de tentativas de login excedido");
                });
    }

    @Test
    @DisplayName("Given limite atingido When reset Then novas tentativas são permitidas")
    void givenLimitReached_whenReset_thenAllowsNewAttempts() {
        String clientKey = "127.0.0.1:user@smartagro.test";

        rateLimiter.checkAndIncrement(clientKey);
        rateLimiter.checkAndIncrement(clientKey);
        rateLimiter.checkAndIncrement(clientKey);

        rateLimiter.reset(clientKey);

        assertThatCode(() -> rateLimiter.checkAndIncrement(clientKey)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Given limitador desabilitado When muitas tentativas Then nenhuma exceção é lançada")
    void givenDisabledLimiter_whenManyAttempts_thenNeverThrows() throws Exception {
        setField("enabled", false);
        String clientKey = "127.0.0.1:user@smartagro.test";

        for (int i = 0; i < 20; i++) {
            rateLimiter.checkAndIncrement(clientKey);
        }
    }

    @Test
    @DisplayName("Given chave nula ou vazia When checkAndIncrement Then ignora sem erro")
    void givenNullOrEmptyKey_whenCheckAndIncrement_thenIgnores() {
        assertThatCode(() -> {
            rateLimiter.checkAndIncrement(null);
            rateLimiter.checkAndIncrement("   ");
        }).doesNotThrowAnyException();
    }
}
