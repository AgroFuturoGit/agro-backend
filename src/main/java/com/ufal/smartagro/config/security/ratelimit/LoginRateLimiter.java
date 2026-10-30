package com.ufal.smartagro.config.security.ratelimit;

import com.ufal.smartagro.domain.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Limitador de taxa em memória para tentativas de login.
 * Garante defesa em profundidade na própria API contra ataques de força bruta,
 * mesmo que o proxy reverso (Nginx) esteja desativado ou mal configurado.
 */
@Component
public class LoginRateLimiter {

    @Value("${app.security.rate-limit.login.enabled:true}")
    private boolean enabled = true;

    @Value("${app.security.rate-limit.login.max-attempts:10}")
    private int maxAttempts = 10;

    @Value("${app.security.rate-limit.login.window-seconds:60}")
    private long windowSeconds = 60;

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Instant>> attempts = new ConcurrentHashMap<>();

    public void checkAndIncrement(String clientKey) {
        if (!enabled || clientKey == null || clientKey.isBlank()) {
            return;
        }

        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(windowSeconds);

        ConcurrentLinkedQueue<Instant> clientAttempts =
                attempts.computeIfAbsent(clientKey, k -> new ConcurrentLinkedQueue<>());

        while (!clientAttempts.isEmpty() && clientAttempts.peek().isBefore(cutoff)) {
            clientAttempts.poll();
        }

        if (clientAttempts.size() >= maxAttempts) {
            Instant oldest = clientAttempts.peek();
            long retryAfter = oldest != null
                    ? Math.max(1, Duration.between(now, oldest.plusSeconds(windowSeconds)).getSeconds())
                    : windowSeconds;
            throw new RateLimitExceededException(
                    "Limite de tentativas de login excedido. Tente novamente mais tarde.", retryAfter);
        }

        clientAttempts.add(now);
    }

    public void reset(String clientKey) {
        if (clientKey != null) {
            attempts.remove(clientKey);
        }
    }
}
