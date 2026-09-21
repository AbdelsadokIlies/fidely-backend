package com.fidely.backend.domain.models.loyalties;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente une transaction de points de fidélité.
 *
 * <p>Une transaction associe un mouvement de points à une fidélité
 * et éventuellement au ticket à l'origine de ce mouvement.</p>
 */
public class LoyaltyTransaction {

    private final UUID id;
    private final UUID loyaltyId;
    private final UUID ticketId;
    private final int points;
    private final String description;
    private final LocalDateTime createdAt;

    /**
     * Crée une nouvelle transaction de points de fidélité.
     *
     * @param id identifiant de la transaction
     * @param loyaltyId identifiant du programme de fidélité associé
     * @param ticketId identifiant du ticket à l'origine de la transaction
     * @param points nombre de points associés à la transaction
     * @param description description de la transaction
     * @param createdAt date de création de la transaction
     * @throws IllegalArgumentException si la fidélité est null,
     * si le nombre de points est inférieur ou égal à zéro,
     * ou si la date de création est null
     */
    public LoyaltyTransaction(
            UUID id,
            UUID loyaltyId,
            UUID ticketId,
            int points,
            String description,
            LocalDateTime createdAt
    ) {
        if (loyaltyId == null) {
            throw new IllegalArgumentException("Loyalty cannot be null");
        }

        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("Created at cannot be null");
        }

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