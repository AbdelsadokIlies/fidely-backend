package com.fidely.backend.domain.models.loyalties.Rewards;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente l'utilisation d'une récompense de fidélité par un client.
 *
 * <p>Une utilisation possède un statut permettant de suivre son cycle de vie,
 * ainsi qu'un token QR utilisé lors de la consommation de la récompense.</p>
 */
public class RewardRedemption {

    private final UUID id;
    private final UUID rewardId;
    private final UUID loyaltyId;
    private final String qrToken;
    private RedemptionStatus status;
    private LocalDateTime redeemedAt;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    /**

     * Crée une nouvelle utilisation de récompense.
     *
     * @param id identifiant de l'utilisation
     * @param rewardId id de la récompense utilisée
     * @param loyaltyId id de la fidélité associée
     * @param qrToken token QR utilisé pour identifier l'utilisation
     * @param status statut initial de l'utilisation
     * @param redeemedAt date à laquelle la récompense a été consommée, ou null
     * @param expiresAt date d'expiration de l'utilisation, ou null
     * @param createdAt date de création de l'utilisation
     */
    public RewardRedemption(
            UUID id,
            UUID rewardId,
            UUID loyaltyId,
            String qrToken,
            RedemptionStatus status,
            LocalDateTime redeemedAt,
            LocalDateTime expiresAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.rewardId = rewardId;
        this.loyaltyId = loyaltyId;
        this.qrToken = qrToken;
        this.status = status;
        this.redeemedAt = redeemedAt;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRewardId() {
        return rewardId;
    }

    public UUID getLoyaltyId() {
        return loyaltyId;
    }

    public String getQrToken() {
        return qrToken;
    }

    public RedemptionStatus getStatus() {
        return status;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**

     * Consomme la récompense à la date indiquée.
     *
     * <p>La récompense doit être en attente et ne doit pas avoir expiré.
     * En cas d'expiration, son statut passe à {@link RedemptionStatus#EXPIRED}.
     * En cas de succès, son statut passe à {@link RedemptionStatus#CONSUMED}
     * et la date de consommation est enregistrée.</p>
     *
     * @param dateTime date à laquelle la récompense est consommée
     * @throws IllegalStateException si la récompense ne peut pas être consommée
     * ou si elle a expiré
     */
    public void consume(LocalDateTime dateTime) {

        if (status != RedemptionStatus.PENDING) {
            throw new IllegalStateException(
                    "Redemption cannot be consumed"
            );
        }

        if (expiresAt != null && dateTime.isAfter(expiresAt)) {
            status = RedemptionStatus.EXPIRED;
            throw new IllegalStateException(
                    "Redemption has expired"
            );
        }

        status = RedemptionStatus.CONSUMED;
        redeemedAt = dateTime;
    }
}
