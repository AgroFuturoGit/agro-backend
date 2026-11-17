package com.ufal.smartagro.config.security.ratelimit;

import com.ufal.smartagro.domain.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

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

    @Value("${app.security.rate-limit.login.max-entries:10000}")
    private int maxEntries = 10_000;

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Instant>> attempts = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> lastCleanup = new AtomicReference<>(Instant.EPOCH);

    public void checkAndIncrement(String clientKey) {
        if (!enabled || clientKey == null || clientKey.isBlank()) {
            return;
        }

        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(windowSeconds);
        maybeCleanup(now, cutoff);

        if (!attempts.containsKey(clientKey) && attempts.size() >= maxEntries) {
            evictExpired(cutoff);
            if (!attempts.containsKey(clientKey) && attempts.size() >= maxEntries) {
                evictOldestEntry();
            }
        }

        ConcurrentLinkedQueue<Instant> clientAttempts =
                attempts.computeIfAbsent(clientKey, k -> new ConcurrentLinkedQueue<>());

        synchronized (clientAttempts) {
            prune(clientAttempts, cutoff);

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
    }

    public void reset(String clientKey) {
        if (clientKey != null) {
            attempts.remove(clientKey);
        }
    }

    int trackedKeyCount() {
        return attempts.size();
    }

    private void maybeCleanup(Instant now, Instant cutoff) {
        Instant previous = lastCleanup.get();
        if (now.isAfter(previous.plusSeconds(windowSeconds)) && lastCleanup.compareAndSet(previous, now)) {
            evictExpired(cutoff);
        }
    }

    private void evictExpired(Instant cutoff) {
        for (String key : attempts.keySet()) {
            attempts.computeIfPresent(key, (k, queue) -> {
                synchronized (queue) {
                    prune(queue, cutoff);
                    return queue.isEmpty() ? null : queue;
                }
            });
        }
    }

    private void evictOldestEntry() {
        String oldestKey = null;
        Instant oldestSeen = Instant.MAX;
        for (Map.Entry<String, ConcurrentLinkedQueue<Instant>> entry : attempts.entrySet()) {
            Instant head = entry.getValue().peek();
            if (head == null) {
                attempts.remove(entry.getKey(), entry.getValue());
                return;
            }
            if (head.isBefore(oldestSeen)) {
                oldestSeen = head;
                oldestKey = entry.getKey();
            }
        }
        if (oldestKey != null) {
            attempts.remove(oldestKey);
        }
    }

    private void prune(ConcurrentLinkedQueue<Instant> queue, Instant cutoff) {
        while (!queue.isEmpty() && queue.peek().isBefore(cutoff)) {
            queue.poll();
        }
    }
}
