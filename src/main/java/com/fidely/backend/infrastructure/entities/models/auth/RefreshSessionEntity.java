package com.fidely.backend.infrastructure.entities.models.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une session de refresh token
 * persistée en base de données.
 *
 * <p>Le refresh token en clair n'est jamais stocké.
 * Seul son hash est conservé.</p>
 *
 * <p>Les sessions appartenant à une même chaîne de rotation
 * partagent le même {@code familyId}.</p>
 */
@Entity
@Table(
        name = "refresh_sessions",
        indexes = {
                @Index(
                        name = "idx_refresh_session_family_id",
                        columnList = "family_id"
                ),
                @Index(
                        name = "idx_refresh_session_user_id",
                        columnList = "user_id"
                )
        }
)
public class RefreshSessionEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructeur requis par JPA.
     */
    protected RefreshSessionEntity() {
    }

    /**
     * Construit une entité de session de refresh.
     *
     * @param id identifiant de la session
     * @param userId identifiant de l'utilisateur
     * @param familyId identifiant de la famille de rotation
     * @param tokenHash hash du refresh token
     * @param expiresAt date d'expiration
     * @param revokedAt date de révocation, éventuellement nulle
     * @param createdAt date de création
     */
    public RefreshSessionEntity(
            UUID id,
            UUID userId,
            UUID familyId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.familyId = familyId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.createdAt = createdAt;
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