package com.ufal.smartagro.config.security.service;

import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtTokenService {
    @Value("${jwt.secret}")
    private  String secret;
    private static final String ISSUER = "smartagro-api";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetailsImpl user) {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(4, ChronoUnit.HOURS)))
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
        } catch (JwtException e) {
            throw new RuntimeException("Token inválido ou expirado.");
        }
    }
}
