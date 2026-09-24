package com.fidely.backend.infrastructure.security;

import com.fidely.backend.application.port.out.security.IEmailVerificationTokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Génère et valide les tokens JWT utilisés pour la vérification
 * des adresses email.
 */
@Component
public class JjwtEmailVerificationTokenGenerator
        implements IEmailVerificationTokenGenerator {

    private static final String PURPOSE_CLAIM =
            "email-verification";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    /**
     * Crée le générateur de tokens de vérification d'adresse email.
     *
     * @param jwtProperties configuration JWT utilisée pour la génération
     *                      des tokens
     */
    public JjwtEmailVerificationTokenGenerator(
            JwtProperties jwtProperties
    ) {
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

        if (jwtProperties
                .getEmailVerificationTokenExpirationMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "Email verification token expiration must be greater than zero"
            );
        }

        this.jwtProperties = jwtProperties;

        this.signingKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Génère un token JWT permettant de vérifier l'adresse email
     * d'un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return token JWT de vérification d'adresse email
     */
    @Override
    public String generate(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        Instant issuedAt = Instant.now();

        Instant expiration = issuedAt.plusSeconds(
                jwtProperties
                        .getEmailVerificationTokenExpirationMinutes() * 60
        );

        return Jwts.builder()
                .subject(userId.toString())
                .claim("purpose", PURPOSE_CLAIM)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extrait l'identifiant de l'utilisateur d'un token de vérification
     * d'adresse email après validation de sa signature et de son usage.
     *
     * @param token token JWT de vérification d'adresse email
     * @return identifiant de l'utilisateur contenu dans le token
     * @throws IllegalArgumentException si le token est invalide
     */
    @Override
    public UUID extractUserId(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Email verification token cannot be null or blank"
            );
        }

        try {
            var claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String purpose = claims.get("purpose", String.class);

            if (!PURPOSE_CLAIM.equals(purpose)) {
                throw new IllegalArgumentException(
                        "Invalid email verification token"
                );
            }

            return UUID.fromString(claims.getSubject());

        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid email verification token",
                    exception
            );
        }
    }
}