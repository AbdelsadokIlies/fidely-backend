package com.fidely.backend.domain.model.users;

import com.fidely.backend.domain.model.Merchants.Merchant;
import com.fidely.backend.domain.model.users.userrole.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente un utilisateur ayant un rôle de gestion sur un marchand.
 *
 * <p>Un gestionnaire est associé à un compte utilisateur, à un marchand
 * et à un rôle définissant ses permissions.</p>
 */
public class MerchantManager {

    private final UUID id;
    private final User user;
    private final Merchant merchant;
    private final UserRole role;
    private final LocalDateTime createdAt;

    /**

     * Crée une nouvelle association entre un utilisateur et un marchand.
     *
     * @param id identifiant du gestionnaire
     * @param user compte utilisateur associé
     * @param merchant marchand géré
     * @param role rôle attribué au gestionnaire
     * @param createdAt date de création de l'association
     */
    public MerchantManager(
            UUID id,
            User user,
            Merchant merchant,
            UserRole role,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.user = user;
        this.merchant = merchant;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
