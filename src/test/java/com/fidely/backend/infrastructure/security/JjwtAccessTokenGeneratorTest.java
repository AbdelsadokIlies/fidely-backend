package com.fidely.backend.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JjwtAccessTokenGeneratorTest {

    private static final String SECRET =
            "this-is-a-test-secret-key-that-is-long-enough-for-hmac";

    private JwtProperties jwtProperties;
    private JjwtAccessTokenManagement generator;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setAccessTokenExpirationMinutes(15);

        generator = new JjwtAccessTokenManagement(jwtProperties);
    }

    @Test
    void shouldGenerateAccessToken() {
        UUID userId = UUID.randomUUID();

        String token =
                generator.generate(userId, "CUSTOMER");

        assertThat(token)
                .isNotBlank();
    }

    @Test
    void shouldGenerateTokenContainingExpectedClaims() {
        UUID userId = UUID.randomUUID();

        String token =
                generator.generate(userId, "CUSTOMER");

        Claims claims = parseClaims(token);

        assertThat(claims.getSubject())
                .isEqualTo(userId.toString());

        assertThat(claims.get("role", String.class))
                .isEqualTo("CUSTOMER");

        assertThat(claims.getIssuedAt())
                .isNotNull();

        assertThat(claims.getExpiration())
                .isNotNull();
    }

    @Test
    void shouldSetExpirationAccordingToConfiguration() {
        UUID userId = UUID.randomUUID();

        Instant beforeGeneration = Instant.now();

        String token =
                generator.generate(userId, "CUSTOMER");

        Instant afterGeneration = Instant.now();

        Claims claims = parseClaims(token);

        Instant issuedAt =
                claims.getIssuedAt().toInstant();

        Instant expiration =
                claims.getExpiration().toInstant();

        assertThat(issuedAt)
                .isBetween(
                        beforeGeneration.minusSeconds(1),
                        afterGeneration.plusSeconds(1)
                );

        assertThat(Duration.between(issuedAt, expiration))
                .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        String firstToken =
                generator.generate(firstUserId, "CUSTOMER");

        String secondToken =
                generator.generate(secondUserId, "CUSTOMER");

        assertThat(firstToken)
                .isNotEqualTo(secondToken);
    }

    @Test
    void shouldRejectNullUserId() {
        assertThatThrownBy(
                () -> generator.generate(null, "CUSTOMER")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID cannot be null");
    }

    @Test
    void shouldRejectNullRole() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(
                () -> generator.generate(userId, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be null or blank");
    }

    @Test
    void shouldRejectBlankRole() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(
                () -> generator.generate(userId, "   ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be null or blank");
    }

    @Test
    void shouldRejectNullProperties() {
        assertThatThrownBy(
                () -> new JjwtAccessTokenManagement(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT properties cannot be null");
    }

    @Test
    void shouldRejectBlankSecret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("   ");

        assertThatThrownBy(
                () -> new JjwtAccessTokenManagement(properties)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT secret cannot be null or blank");
    }

    @Test
    void shouldRejectInvalidExpiration() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenExpirationMinutes(0);

        assertThatThrownBy(
                () -> new JjwtAccessTokenManagement(properties)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Access token expiration must be greater than zero"
                );
    }

    private Claims parseClaims(String token) {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}