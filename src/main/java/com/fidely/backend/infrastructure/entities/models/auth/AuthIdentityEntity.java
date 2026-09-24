package com.fidely.backend.infrastructure.entities.models.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Entité JPA représentant une identité d'authentification
 * en base de données.
 *
 * <p>Une identité d'authentification est associée à un utilisateur
 * et correspond à un fournisseur d'authentification tel que
 * l'authentification par e-mail, Google ou Apple.</p>
 */
@Entity
@Table(
        name = "auth_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_auth_identity_provider_user",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
public class AuthIdentityEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**

     * Constructeur utilisé par JPA.
     */
    protected AuthIdentityEntity() {
    }

    /**

     * Crée une nouvelle entité d'identité d'authentification.
     *
     * @param id identifiant de l'identité
     * @param userId identifiant de l'utilisateur associé
     * @param provider fournisseur d'authentification
     * @param providerUserId identifiant de l'utilisateur auprè du fournisseu
     * @param passwordHash hash du mot de passe
     * @param createdAt date de création de l'identité
     */
    public AuthIdentityEntity(
            UUID id,
            UUID userId,
            String provider,
            String providerUserId,
            String passwordHash,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
