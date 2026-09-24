package com.fidely.backend.domain.models.users;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente un utilisateur ayant un rôle de gestion sur un marchand.
 *
 * <p>Un gestionnaire est associé à un compte utilisateur et à un marchand.
 * Son type concret {@link MerchantManager} représente son rôle de gestionnaire
 * de marchand dans le domaine.</p>
 *
 * <p>Les informations relatives à l'authentification sont gérées
 * séparément du compte utilisateur.</p>
 */
public class MerchantManager extends User {

    private final UUID merchantId;

    /**
     * Crée un nouveau manager de marchand.
     *
     * @param id identifiant de l'utilisateur
     * @param merchantId identifiant du marchand associé
     * @param email adresse e-mail du manager
     * @param firstName prénom du manager
     * @param lastName nom du manager
     * @param emailVerified indique si l'e-mail a été vérifié
     * @param active indique si l'utilisateur est actif
     * @param createdAt date de création du compte
     * @param updatedAt date de dernière modification du compte
     */
    public MerchantManager(
            UUID id,
            UUID merchantId,
            String email,
            String firstName,
            String lastName,
            boolean emailVerified,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super(
                id,
                email,
                firstName,
                lastName,
                emailVerified,
                active,
                createdAt,
                updatedAt
        );
        this.merchantId = merchantId;
    }

    /**
     * Retourne l'identifiant du marchand associé.
     *
     * @return identifiant du marchand associé
     */
    public UUID getMerchantId() {
        return merchantId;
    }
}
