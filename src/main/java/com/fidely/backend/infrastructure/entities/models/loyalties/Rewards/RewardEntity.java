package com.fidely.backend.infrastructure.entities.models.loyalties.Rewards;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une récompense de fidélité.
 *
 * <p>Cette entité correspond à la table {@code rewards}.</p>
 */
@Entity
@Table(name = "rewards")
public class RewardEntity {

    @Id
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "cost_points", nullable = false)
    private int costPoints;

    @Column(name = "quantity_available")
    private Integer quantityAvailable;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructeur requis par JPA.
     */
    protected RewardEntity() {
    }

    public RewardEntity(
            UUID id,
            UUID merchantId,
            String name,
            String description,
            int costPoints,
            Integer quantityAvailable,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            boolean active,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.name = name;
        this.description = description;
        this.costPoints = costPoints;
        this.quantityAvailable = quantityAvailable;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = active;
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

    public String getDescription() {
        return description;
    }

    public int getCostPoints() {
        return costPoints;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}