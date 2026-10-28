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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtTokenService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-hours:4}")
    private long expirationHours = 4;

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

    public String generateToken(UserDetailsImpl user) {
        long hours = expirationHours > 0 ? expirationHours : 4;
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getUser().getRole().name())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(hours, ChronoUnit.HOURS)))
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
