package com.fidely.backend.domain.models.auth;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente une session de refresh token dans le domaine Fidely.
 *
 * <p>Chaque session correspond à un refresh token stocké sous
 * forme de hash. Les sessions appartenant à une même chaîne
 * de rotation partagent le même identifiant de famille.</p>
 *
 * <p>Le token en clair n'est jamais conservé dans le domaine
 * ni en base de données.</p>
 */
public class RefreshSession {

    private final UUID id;
    private final UUID userId;
    private final UUID familyId;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private final LocalDateTime createdAt;

    /**
     * Construit une session de refresh token.
     *
     * @param id identifiant de la session
     * @param userId identifiant de l'utilisateur
     * @param familyId identifiant de la famille de rotation
     * @param tokenHash hash du refresh token
     * @param expiresAt date d'expiration
     * @param revokedAt date de révocation, éventuellement nulle
     * @param createdAt date de création
     */
    public RefreshSession(
            UUID id,
            UUID userId,
            UUID familyId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt,
            LocalDateTime createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Refresh session ID cannot be null"
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        if (familyId == null) {
            throw new IllegalArgumentException(
                    "Family ID cannot be null"
            );
        }

        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Token hash cannot be null or blank"
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Expires at cannot be null"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Created at cannot be null"
            );
        }

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "Expires at must be after created at"
            );
        }

        if (revokedAt != null && revokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Revoked at cannot be before created at"
            );
        }

        this.id = id;
        this.userId = userId;
        this.familyId = familyId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
    }

    /**
     * Indique si la session est expirée à une date donnée.
     *
     * @param now date de référence
     * @return true si la session est expirée
     */
    public boolean isExpiredAt(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException(
                    "Reference date cannot be null"
            );
        }

        return !now.isBefore(expiresAt);
    }

    /**
     * Indique si la session a été révoquée.
     *
     * @return true si la session est révoquée
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * Indique si la session peut encore être utilisée.
     *
     * @param now date de référence
     * @return true si la session est active et non expirée
     */
    public boolean isUsableAt(LocalDateTime now) {
        return !isRevoked() && !isExpiredAt(now);
    }

    /**
     * Révoque la session à la date indiquée.
     *
     * @param revokedAt date de révocation
     */
    public void revoke(LocalDateTime revokedAt) {
        if (revokedAt == null) {
            throw new IllegalArgumentException(
                    "Revoked at cannot be null"
            );
        }

        if (revokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Revoked at cannot be before created at"
            );
        }

        if (isRevoked()) {
            throw new IllegalStateException(
                    "Refresh session is already revoked"
            );
        }

        this.revokedAt = revokedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}