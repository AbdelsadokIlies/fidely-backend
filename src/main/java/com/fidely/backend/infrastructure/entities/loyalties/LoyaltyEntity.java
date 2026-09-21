package com.fidely.backend.infrastructure.entities.loyalties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un programme de fidélité en base de données.
 */
@Entity
@Table(
        name = "loyalties",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_loyalty_customer_merchant",
                        columnNames = {"customer_id", "merchant_id"}
                )
        }
)
public class LoyaltyEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "points_balance", nullable = false)
    private int pointsBalance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Constructeur utilisé par JPA.
     */
    protected LoyaltyEntity() {
    }

    /**
     * Crée une nouvelle entité de programme de fidélité.
     *
     * @param id identifiant du programme
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @param pointsBalance solde de points
     * @param createdAt date de création
     * @param updatedAt date de dernière modification
     */
    public LoyaltyEntity(
            UUID id,
            UUID customerId,
            UUID merchantId,
            int pointsBalance,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.merchantId = merchantId;
        this.pointsBalance = pointsBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setPointsBalance(int pointsBalance) {
        this.pointsBalance = pointsBalance;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}