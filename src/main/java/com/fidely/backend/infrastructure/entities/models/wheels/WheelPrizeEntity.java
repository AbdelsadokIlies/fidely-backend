package com.fidely.backend.infrastructure.entities.models.wheels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant un lot associé à une roue.
 */
@Entity
@Table(name = "wheel_prizes")
public class WheelPrizeEntity {

    @Id
    private UUID id;

    @Column(name = "wheel_id", nullable = false)
    private UUID wheelId;

    @Column(nullable = false)
    private String label;

    @Column(name = "probability_weight", nullable = false)
    private int probabilityWeight;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected WheelPrizeEntity() {
    }

    public WheelPrizeEntity(
            UUID id,
            UUID wheelId,
            String label,
            int probabilityWeight,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.wheelId = wheelId;
        this.label = label;
        this.probabilityWeight = probabilityWeight;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWheelId() {
        return wheelId;
    }

    public String getLabel() {
        return label;
    }

    public int getProbabilityWeight() {
        return probabilityWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}