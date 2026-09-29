package com.fidely.backend.infrastructure.entities.models.wheels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une participation à une roue de récompenses.
 */
@Entity
@Table(name = "wheel_participations")
public class WheelParticipationEntity {

    @Id
    private UUID id;

    @Column(name = "wheel_id", nullable = false)
    private UUID wheelId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "wheel_prize_id")
    private UUID wheelPrizeId;

    @Column(name = "result_label")
    private String resultLabel;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    protected WheelParticipationEntity() {
    }

    public WheelParticipationEntity(
            UUID id,
            UUID wheelId,
            UUID customerId,
            UUID ticketId,
            UUID wheelPrizeId,
            String resultLabel,
            LocalDateTime playedAt
    ) {
        this.id = id;
        this.wheelId = wheelId;
        this.customerId = customerId;
        this.ticketId = ticketId;
        this.wheelPrizeId = wheelPrizeId;
        this.resultLabel = resultLabel;
        this.playedAt = playedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWheelId() {
        return wheelId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public UUID getWheelPrizeId() {
        return wheelPrizeId;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }
}