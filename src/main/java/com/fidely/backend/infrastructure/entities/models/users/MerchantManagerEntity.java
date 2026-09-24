package com.fidely.backend.infrastructure.entities.models.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entité JPA représentant le profil spécifique d'un gestionnaire
 * de marchand.
 *
 * <p>Le profil complète les informations communes stockées
 * dans {@link UserEntity}. Il associe également l'utilisateur
 * au marchand qu'il est autorisé à gérer.</p>
 *
 * <p>L'identifiant du profil correspond directement à l'identifiant
 * de l'utilisateur associé.</p>
 */
@Entity
@Table(name = "merchant_managers")
public class MerchantManagerEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /**
     * Constructeur requis par JPA.
     */
    protected MerchantManagerEntity() {
    }

    /**
     * Construit un profil de gestionnaire de marchand.
     *
     * @param userId identifiant de l'utilisateur associé
     * @param merchantId identifiant du marchand géré
     */
    public MerchantManagerEntity(
            UUID userId,
            UUID merchantId
    ) {
        this.userId = userId;
        this.merchantId = merchantId;
    }

    /**
     * Retourne l'identifiant de l'utilisateur associé.
     *
     * @return identifiant utilisateur
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Retourne l'identifiant du marchand géré.
     *
     * @return identifiant du marchand
     */
    public UUID getMerchantId() {
        return merchantId;
    }
}