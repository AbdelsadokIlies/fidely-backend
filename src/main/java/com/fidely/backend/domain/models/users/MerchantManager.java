package com.fidely.backend.domain.models.users;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente un utilisateur ayant un rôle de gestion sur un marchand.
 *
 * <p>Un gestionnaire est associé à un compte utilisateur, à un marchand
 * et à un rôle définissant ses permissions.</p>
 */
public class MerchantManager extends User {

    private final UUID merchantId;

    /**
     * Crée un nouveau manager de marchand.
     *
     * @param id identifiant du marchand
     * @param merchantId id de la société du marchand
     * @param email email du marchand
     * @param firstName prénom du marchand
     * @param lastName nom du marchand
     * @param passwordHash mot de passe haché du marchand
     * @param emailVerified indique si l'email a été vérifié
     * @param active indique si l'utilisateur est actif
     * @param createdAt date de création du marchand
     * @param updatedAt date de dernière modification du marchand
     */
    public MerchantManager(
            UUID id,
            UUID merchantId,
            String email,
            String firstName,
            String lastName,
            String passwordHash,
            boolean emailVerified,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super(
                id,
                email,
                passwordHash,
                firstName,
                lastName,
                emailVerified,
                active,
                createdAt,
                updatedAt
        );
        this.merchantId = merchantId;
    }

    public UUID getMerchantId() {
        return merchantId;
    }
}