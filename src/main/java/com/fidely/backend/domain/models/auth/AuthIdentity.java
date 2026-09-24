package com.fidely.backend.domain.models.auth;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente une identité d'authentification associée à un utilisateur
 * de la plateforme Fidely.
 *
 * <p>Une identité définit une méthode permettant à un utilisateur
 * de s'authentifier auprès de la plateforme. Elle peut correspondre
 * à une authentification par e-mail et mot de passe ou à un fournisseur
 * externe tel que Google ou Apple.</p>
 *
 * <p>Le mot de passe haché est stocké uniquement pour les identités
 * utilisant une authentification par mot de passe. Les fournisseurs
 * externes utilisent leur propre identifiant de fournisseur.</p>
 */
public class AuthIdentity {

    private final UUID id;
    private final UUID userId;
    private final String provider;
    private final String providerUserId;
    private final String passwordHash;
    private final LocalDateTime createdAt;

    /**
     * Crée une nouvelle identité d'authentification.
     *
     * @param id identifiant de l'identité
     * @param userId identifiant de l'utilisateur associé
     * @param provider fournisseur d'authentification
     * @param providerUserId identifiant de l'utilisateur auprès du fournisseur
     * @param passwordHash hash du mot de passe lorsque l'authentification
     * utilise un mot de passe
     * @param createdAt date de création de l'identité
     */
    public AuthIdentity(
            UUID id,
            UUID userId,
            String provider,
            String providerUserId,
            String passwordHash,
            LocalDateTime createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Auth identity ID cannot be null"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider cannot be null or blank"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Created at cannot be null"
            );
        }

        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    /**
     * Crée une nouvelle identité avec un nouveau hash de mot de passe.
     *
     * <p>L'identité actuelle reste inchangée car le modèle est immutable.</p>
     *
     * @param newPasswordHash nouveau hash du mot de passe
     * @return nouvelle identité avec le nouveau hash
     */
    public AuthIdentity withPasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be null or blank"
            );
        }

        return new AuthIdentity(
                this.id,
                this.userId,
                this.provider,
                this.providerUserId,
                newPasswordHash,
                this.createdAt
        );
    }

    /**
     * Retourne l'identifiant de l'identité d'authentification.
     *
     * @return identifiant de l'identité d'authentification
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retourne l'identifiant de l'utilisateur associé.
     *
     * @return identifiant de l'utilisateur associé
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Retourne le fournisseur d'authentification.
     *
     * @return fournisseur d'authentification
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Retourne l'identifiant de l'utilisateur auprès du fournisseur.
     *
     * @return identifiant de l'utilisateur auprès du fournisseur,
     * ou {@code null} lorsqu'il n'est pas applicable
     */
    public String getProviderUserId() {
        return providerUserId;
    }

    /**
     * Retourne le hash du mot de passe.
     *
     * @return hash du mot de passe, ou {@code null} lorsque
     * l'identité n'utilise pas de mot de passe
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Retourne la date de création de l'identité.
     *
     * @return date de création de l'identité
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}