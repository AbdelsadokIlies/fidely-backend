package com.fidely.backend.domain.model.users;

import com.fidely.backend.domain.model.users.userrole.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente le compte utilisateur de la plateforme Fidely.
 *
 * <p>Cette classe constitue la base commune des différents types d'utilisateurs.
 * Elle contient les informations d'authentification, le rôle et l'état du compte.</p>
 */
public abstract class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final boolean emailVerified;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**

     * Crée un nouvel utilisateur.
     *
     * @param id identifiant de l'utilisateur
     * @param email adresse e-mail de l'utilisateur
     * @param passwordHash hash du mot de passe de l'utilisateur
     * @param role rôle de l'utilisateur
     * @param emailVerified indique si l'adresse e-mail a été vérifiée
     * @param active indique si le compte est actif
     * @param createdAt date de création du compte
     * @param updatedAt date de dernière modification du compte
     */
    public User(
            UUID id,
            String email,
            String passwordHash,
            UserRole role,
            boolean emailVerified,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = emailVerified;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
