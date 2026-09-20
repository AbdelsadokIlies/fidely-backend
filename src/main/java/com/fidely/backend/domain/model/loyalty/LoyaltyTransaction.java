package com.fidely.backend.domain.model.loyalty;

import com.fidely.backend.domain.model.tickets.Ticket;

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
    private final Loyalty loyalty;
    private final Ticket ticket;
    private final int points;
    private final String description;
    private final LocalDateTime createdAt;

    /**

     * Crée une nouvelle transaction de fidélité.
     *
     * @param id identifiant de la transaction
     * @param loyalty fidélité associée à la transaction
     * @param ticket ticket à l'origine de la transaction
     * @param points nombre de points gagnés ou dépensés
     * @param description description de la transaction
     * @param createdAt date de création de la transaction
     */
    public LoyaltyTransaction(
            UUID id,
            Loyalty loyalty,
            Ticket ticket,
            int points,
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.loyalty = loyalty;
        this.ticket = ticket;
        this.points = points;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Loyalty getLoyalty() {
        return loyalty;
    }

    public Ticket getTicket() {
        return ticket;
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
