package com.fidely.backend.domain.models.wheels;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente une roue de récompenses associée à un marchand.
 *
 * <p>Une roue définit les conditions permettant à un client de participer,
 * notamment l'intervalle minimal entre deux participations et la nécessité
 * de disposer d'un achat validé.</p>
 */
public class Wheel {

    private final UUID id;
    private final UUID merchantId;
    private final String name;
    private final boolean active;
    private final int minIntervalMinutes;
    private final boolean requiresValidatedPurchase;
    private final LocalDateTime createdAt;

    /**

     * Crée une nouvelle roue de récompenses.
     *
     * @param id identifiant de la roue
     * @param merchantId id de la société associée à la roue
     * @param name nom de la roue
     * @param active indique si la roue est active
     * @param minIntervalMinutes intervalle minimal en minutes entre deux participations
     * @param requiresValidatedPurchase indique si un achat validé est requis pour participer
     * @param createdAt date de création de la roue
     */
    public Wheel(
            UUID id,
            UUID merchantId,
            String name,
            boolean active,
            int minIntervalMinutes,
            boolean requiresValidatedPurchase,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.name = name;
        this.active = active;
        this.minIntervalMinutes = minIntervalMinutes;
        this.requiresValidatedPurchase = requiresValidatedPurchase;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public int getMinIntervalMinutes() {
        return minIntervalMinutes;
    }

    public boolean isRequiresValidatedPurchase() {
        return requiresValidatedPurchase;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
