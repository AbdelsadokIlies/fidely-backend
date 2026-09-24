package com.fidely.backend.infrastructure.entities.models.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant le compte utilisateur commun
 * de la plateforme Fidely.
 *
 * <p>Cette entité contient uniquement les informations communes
 * à tous les types d'utilisateurs. Les informations spécifiques
 * aux clients et aux gestionnaires de marchands sont stockées
 * dans leurs tables de profil respectives.</p>
 *
 * <p>Les informations relatives à l'authentification, notamment
 * le mot de passe, ne sont pas stockées dans cette entité.
 * Elles sont gérées par {@code AuthIdentityEntity}.</p>
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructeur requis par JPA.
     */
    protected UserEntity() {
    }

    /**
     * Construit une entité utilisateur.
     *
     * @param id identifiant de l'utilisateur
     * @param email adresse e-mail de l'utilisateur
     * @param firstName prénom de l'utilisateur
     * @param lastName nom de l'utilisateur
     * @param emailVerified indique si l'adresse e-mail est vérifiée
     * @param active indique si le compte est actif
     * @param createdAt date et heure de création du compte
     * @param updatedAt date et heure de dernière modification du compte
     */
    public UserEntity(
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
     * @return adresse e-mail
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retourne le prénom de l'utilisateur.
     *
     * @return prénom
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Retourne le nom de l'utilisateur.
     *
     * @return nom
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Indique si l'adresse e-mail est vérifiée.
     *
     * @return {@code true} si l'adresse e-mail est vérifiée
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Indique si le compte est actif.
     *
     * @return {@code true} si le compte est actif
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Retourne la date de création du compte.
     *
     * @return date de création
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Retourne la date de dernière modification du compte.
     *
     * @return date de modification
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}