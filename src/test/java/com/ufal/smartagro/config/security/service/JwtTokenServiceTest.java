package com.ufal.smartagro.config.security.service;

import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.testsupport.UserTestFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class JwtTokenServiceTest {

    private static final String SECRET = "smartagro-test-secret-key-32bytes!!";
    private static final String ISSUER = "smartagro-api";

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenService = new JwtTokenService();
        setField(jwtTokenService, "secret", SECRET);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("Given UserDetails válido When generateToken")
    class GenerateToken {

        @Test
        @DisplayName("Then emite JWT com issuer, subject (email), id, name, role e expiração de 4h por padrão")
        void givenAdminDetails_whenGenerateToken_thenContainsExpectedClaims() {
            UUID userId = UUID.randomUUID();
            UserDetailsImpl details = new UserDetailsImpl(
                    UserTestFactory.user()
                            .id(userId)
                            .admin()
                            .fullName("Admin Sistema")
                            .email("admin@smartagro.test")
                            .toEntity()
            );

            String token = jwtTokenService.generateToken(details);

            Claims claims = parseClaims(token);
            assertThat(claims.getIssuer()).isEqualTo(ISSUER);
            assertThat(claims.getSubject()).isEqualTo("admin@smartagro.test");
            assertThat(claims.get("id", String.class)).isEqualTo(userId.toString());
            assertThat(claims.get("name", String.class)).isEqualTo("Admin Sistema");
            assertThat(claims.get("role", String.class)).isEqualTo(Role.ADMIN.name());
            assertThat(claims.getExpiration().toInstant())
                    .isCloseTo(Instant.now().plus(4, ChronoUnit.HOURS), within(15, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Then respeita jwt.expiration quando configurado explicitamente")
        void givenCustomExpiration_whenGenerateToken_thenUsesCustomDuration() throws Exception {
            setField(jwtTokenService, "expiration", "2h");

            UserDetailsImpl details = new UserDetailsImpl(
                    UserTestFactory.user().technician().email("tech@smartagro.test").toEntity()
            );

            String token = jwtTokenService.generateToken(details);
            Claims claims = parseClaims(token);

            assertThat(claims.getExpiration().toInstant())
                    .isCloseTo(Instant.now().plus(2, ChronoUnit.HOURS), within(15, ChronoUnit.SECONDS));
        }

        @ParameterizedTest(name = "Then claim role reflete Role.{0}")
        @EnumSource(Role.class)
        void givenEachRole_whenGenerateToken_thenRoleClaimMatches(Role role) {
            UserDetailsImpl details = new UserDetailsImpl(
                    UserTestFactory.user().role(role).email(role.name().toLowerCase() + "@smartagro.test").toEntity()
            );

            Claims claims = parseClaims(jwtTokenService.generateToken(details));

            assertThat(claims.get("role", String.class)).isEqualTo(role.name());
            assertThat(claims.getSubject()).isEqualTo(details.getUsername());
        }
    }

    @Nested
    @DisplayName("Given diferentes formatos de jwt.expiration")
    class ExpirationParsing {

        @ParameterizedTest
        @CsvSource({
                "1d, 86400",
                "2h, 7200",
                "30m, 1800",
                "60s, 60",
                "PT3H, 10800",
                "3600, 3600"
        })
        @DisplayName("Then calcula duração correta para formatos suportados")
        void givenSupportedFormats_whenResolveDuration_thenReturnsExpectedSeconds(String expression, long expectedSeconds) throws Exception {
            setField(jwtTokenService, "expiration", expression);
            Duration duration = jwtTokenService.resolveExpirationDuration();
            assertThat(duration.getSeconds()).isEqualTo(expectedSeconds);
        }

        @Test
        @DisplayName("Then fallback para expirationHours quando jwt.expiration for em branco")
        void givenEmptyExpiration_whenResolveDuration_thenFallbacksToExpirationHours() throws Exception {
            setField(jwtTokenService, "expiration", "");
            setField(jwtTokenService, "expirationHours", 8L);

            Duration duration = jwtTokenService.resolveExpirationDuration();
            assertThat(duration.toHours()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("Given um token When getSubjectFromToken")
    class ParseToken {

        @Test
        @DisplayName("Then retorna o email (subject) de um token válido")
        void givenValidToken_whenGetSubject_thenReturnsEmail() {
            UserDetailsImpl details = new UserDetailsImpl(
                    UserTestFactory.user().technician().email("technician@smartagro.test").toEntity()
            );
            String token = jwtTokenService.generateToken(details);

            String subject = jwtTokenService.getSubjectFromToken(token);

            assertThat(subject).isEqualTo("technician@smartagro.test");
        }

        @Test
        @DisplayName("Then lança RuntimeException quando o token é malformado")
        void givenMalformedToken_whenGetSubject_thenThrowsRuntimeException() {
            assertThatThrownBy(() -> jwtTokenService.getSubjectFromToken("token-invalido"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Token inválido ou expirado.");
        }

        @Test
        @DisplayName("Then lança RuntimeException quando o issuer é diferente")
        void givenWrongIssuer_whenGetSubject_thenThrowsRuntimeException() {
            String foreignToken = Jwts.builder()
                    .issuer("outro-issuer")
                    .subject("admin@smartagro.test")
                    .issuedAt(new Date())
                    .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .signWith(signingKey())
                    .compact();

            assertThatThrownBy(() -> jwtTokenService.getSubjectFromToken(foreignToken))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Token inválido ou expirado.");
        }

        @Test
        @DisplayName("Then lança RuntimeException quando o token está expirado")
        void givenExpiredToken_whenGetSubject_thenThrowsRuntimeException() {
            String expired = Jwts.builder()
                    .issuer(ISSUER)
                    .subject("admin@smartagro.test")
                    .issuedAt(Date.from(Instant.now().minus(5, ChronoUnit.HOURS)))
                    .expiration(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                    .signWith(signingKey())
                    .compact();

            assertThatThrownBy(() -> jwtTokenService.getSubjectFromToken(expired))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Token inválido ou expirado.");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
