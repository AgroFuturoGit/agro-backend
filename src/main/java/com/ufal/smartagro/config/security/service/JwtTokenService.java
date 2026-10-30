package com.ufal.smartagro.config.security.service;

import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-hours:4}")
    private long expirationHours = 4;

    @Value("${jwt.expiration:}")
    private String expiration;

    private static final String ISSUER = "smartagro-api";
    private SecretKey cachedSigningKey;

    @PostConstruct
    void validateAndInitKey() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("A propriedade jwt.secret (JWT_SECRET) deve ter pelo menos 32 bytes.");
        }
        this.cachedSigningKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getSigningKey() {
        if (cachedSigningKey == null) {
            cachedSigningKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        return cachedSigningKey;
    }

    Duration resolveExpirationDuration() {
        if (expiration != null && !expiration.isBlank()) {
            String trimmed = expiration.trim();
            try {
                if (trimmed.startsWith("PT") || trimmed.startsWith("pt")) {
                    return Duration.parse(trimmed);
                }
                char lastChar = Character.toLowerCase(trimmed.charAt(trimmed.length() - 1));
                if (Character.isLetter(lastChar)) {
                    long amount = Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim());
                    return switch (lastChar) {
                        case 'd' -> Duration.ofDays(amount);
                        case 'h' -> Duration.ofHours(amount);
                        case 'm' -> Duration.ofMinutes(amount);
                        case 's' -> Duration.ofSeconds(amount);
                        default -> Duration.ofHours(amount);
                    };
                }
                long numeric = Long.parseLong(trimmed);
                if (numeric > 86400L * 30L) {
                    return Duration.ofMillis(numeric);
                } else {
                    return Duration.ofSeconds(numeric);
                }
            } catch (Exception ignored) {
                // fallback to expirationHours
            }
        }
        long hours = expirationHours > 0 ? expirationHours : 4;
        return Duration.ofHours(hours);
    }

    public String generateToken(UserDetailsImpl user) {
        Duration duration = resolveExpirationDuration();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getUser().getRole().name())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(duration)))
                .signWith(getSigningKey())
                .compact();
    }

    public String getSubjectFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Token inválido ou expirado.");
        }
    }
}
