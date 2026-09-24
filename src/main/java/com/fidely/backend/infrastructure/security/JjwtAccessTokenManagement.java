package com.fidely.backend.infrastructure.security;

import com.fidely.backend.application.port.out.security.IAccessTokenManagement;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Implémentation de la gestion des access tokens utilisant JWT.
 *
 * <p>Les tokens sont signés avec une clé HMAC configurée
 * dans {@link JwtProperties}. Ils contiennent l'identifiant
 * de l'utilisateur, son rôle ainsi que leurs dates de création
 * et d'expiration.</p>
 *
 * <p>La clé secrète et la durée de validité ne sont jamais
 * codées en dur dans cette classe.</p>
 */
@Component
public class JjwtAccessTokenManagement
        implements IAccessTokenManagement {

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    /**
     * Construit le gestionnaire JWT à partir de sa configuration.
     *
     * @param jwtProperties propriétés JWT
     */
    public JjwtAccessTokenManagement(JwtProperties jwtProperties) {
        if (jwtProperties == null) {
            throw new IllegalArgumentException(
                    "JWT properties cannot be null"
            );
        }

        if (jwtProperties.getSecret() == null
                || jwtProperties.getSecret().isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret cannot be null or blank"
            );
        }

        if (jwtProperties.getAccessTokenExpirationMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "Access token expiration must be greater than zero"
            );
        }

        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Génère un access token JWT pour un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @param role rôle applicatif de l'utilisateur
     * @return JWT signé
     */
    @Override
    public String generate(UUID userId, String role) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Role cannot be null or blank"
            );
        }

        Instant issuedAt = Instant.now();

        Instant expiration = issuedAt.plusSeconds(
                jwtProperties.getAccessTokenExpirationMinutes() * 60
        );

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extrait l'identifiant utilisateur d'un access token valide.
     *
     * @param accessToken access token JWT
     * @return identifiant de l'utilisateur
     */
    @Override
    public UUID extractUserId(String accessToken) {
        Claims claims = parseClaims(accessToken);

        try {
            return UUID.fromString(claims.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid access token",
                    exception
            );
        }
    }

    /**
     * Extrait le rôle d'un access token valide.
     *
     * @param accessToken access token JWT
     * @return rôle de l'utilisateur
     */
    @Override
    public String extractRole(String accessToken) {
        Claims claims = parseClaims(accessToken);

        String role = claims.get("role", String.class);

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid access token"
            );
        }

        return role;
    }

    /**
     * Parse et valide un access token JWT.
     *
     * <p>La signature et l'expiration du token sont vérifiées
     * par JJWT.</p>
     *
     * @param accessToken access token JWT
     * @return claims du token
     */
    private Claims parseClaims(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token cannot be null or blank"
            );
        }

        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid access token",
                    exception
            );
        }
    }
}