package com.fidely.backend.domain.models.users;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente le compte utilisateur de la plateforme Fidely.
 *
 * <p>Cette classe constitue la base commune des différents types
 * d'utilisateurs. Elle contient les informations d'identité et
 * l'état du compte.</p>
 *
 * <p>Les informations relatives aux méthodes d'authentification,
 * telles que le mot de passe, sont gérées séparément par les
 * identités d'authentification.</p>
 */
public abstract class User {

    private final UUID id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private boolean emailVerified;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Crée un nouvel utilisateur.
     *
     * @param id identifiant de l'utilisateur
     * @param email adresse e-mail de l'utilisateur
     * @param firstName prénom de l'utilisateur
     * @param lastName nom de l'utilisateur
     * @param emailVerified indique si l'adresse e-mail a été vérifiée
     * @param active indique si le compte est actif
     * @param createdAt date de création du compte
     * @param updatedAt date de dernière modification du compte
     */
    protected User(
            UUID id,
            String email,
            String firstName,
            String lastName,
            boolean emailVerified,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailVerified = emailVerified;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Retourne l'identifiant de l'utilisateur.
     *
     * @return identifiant de l'utilisateur
     */
    public UUID getId() {
        return id;
    }

    /**
     * Retourne l'adresse e-mail de l'utilisateur.
     *
     * @return adresse e-mail de l'utilisateur
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retourne le prénom de l'utilisateur.
     *
     * @return prénom de l'utilisateur
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Retourne le nom de l'utilisateur.
     *
     * @return nom de l'utilisateur
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Indique si l'adresse e-mail de l'utilisateur est vérifiée.
     *
     * @return {@code true} si l'adresse e-mail est vérifiée
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Marque l'adresse e-mail comme vérifiée.
     */
    public void verifyEmail() {
        this.emailVerified = true;
    }

    /**
     * Indique si le compte utilisateur est actif.
     *
     * @return {@code true} si le compte est actif
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Retourne la date de création du compte.
     *
     * @return date de création du compte
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Retourne la date de dernière modification du compte.
     *
     * @return date de dernière modification du compte
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
