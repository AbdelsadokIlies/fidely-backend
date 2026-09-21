package com.fidely.backend.infrastructure.entities.loyalties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une transaction de points de fidélité.
 */
@Entity
@Table(name = "loyalty_transactions")
public class LoyaltyTransactionEntity {

    @Id
    private UUID id;

    @Column(name = "loyalty_id", nullable = false)
    private UUID loyaltyId;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructeur utilisé par JPA.
     */
    protected LoyaltyTransactionEntity() {
        // Required by JPA
    }

    /**
     * Crée une nouvelle entité de transaction de points.
     *
     * @param id identifiant de la transaction
     * @param loyaltyId identifiant du programme de fidélité
     * @param ticketId identifiant du ticket associé
     * @param points nombre de points de la transaction
     * @param description description de la transaction
     * @param createdAt date de création de la transaction
     */
    public LoyaltyTransactionEntity(
            UUID id,
            UUID loyaltyId,
            UUID ticketId,
            int points,
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.loyaltyId = loyaltyId;
        this.ticketId = ticketId;
        this.points = points;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLoyaltyId() {
        return loyaltyId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public int getPoints() {
        return points;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}