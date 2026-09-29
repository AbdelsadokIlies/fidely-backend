package com.fidely.backend.infrastructure.entities.models.wheels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une roue de récompenses.
 */
@Entity
@Table(name = "wheels")
public class WheelEntity {

    @Id
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "min_interval_minutes", nullable = false)
    private int minIntervalMinutes;

    @Column(name = "requires_validated_purchase", nullable = false)
    private boolean requiresValidatedPurchase;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected WheelEntity() {
    }

    public WheelEntity(
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