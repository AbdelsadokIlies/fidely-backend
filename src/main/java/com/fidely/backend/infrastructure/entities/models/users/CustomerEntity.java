package com.fidely.backend.infrastructure.entities.models.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entité JPA représentant le profil spécifique d'un client.
 *
 * <p>Le profil client complète les informations communes stockées
 * dans {@link UserEntity}. L'identifiant du profil correspond directement
 * à l'identifiant de l'utilisateur associé.</p>
 *
 * <p>La relation entre l'utilisateur et son profil est donc une relation
 * un-à-un basée sur la même clé primaire.</p>
 */
@Entity
@Table(name = "customer_profiles")
public class CustomerEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Constructeur requis par JPA.
     */
    protected CustomerEntity() {
    }

    /**
     * Construit un profil client.
     *
     * @param userId identifiant de l'utilisateur associé
     * @param phone numéro de téléphone du client
     * @param birthDate date de naissance du client
     */
    public CustomerEntity(
            UUID userId,
            String phone,
            LocalDate birthDate
    ) {
        this.userId = userId;
        this.phone = phone;
        this.birthDate = birthDate;
    }

    /**
     * Retourne l'identifiant de l'utilisateur associé au profil.
     *
     * @return identifiant utilisateur
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Retourne le numéro de téléphone du client.
     *
     * @return numéro de téléphone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Retourne la date de naissance du client.
     *
     * @return date de naissance
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }
}