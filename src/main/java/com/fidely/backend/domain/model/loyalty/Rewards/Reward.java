package com.fidely.backend.domain.model.loyalty.Rewards;

import com.fidely.backend.domain.model.Merchants.Merchant;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente une récompense de fidélité proposée par un marchand.
 *
 * <p>Une récompense peut avoir un coût en points, une quantité disponible
 * et une période de validité.</p>
 */
public class Reward {

    private final UUID id;
    private final Merchant merchant;
    private final String name;
    private final String description;
    private final int costPoints;
    private final Integer quantityAvailable;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final boolean active;
    private final LocalDateTime createdAt;

    /**

     * Crée une nouvelle récompense de fidélité.
     *
     * @param id identifiant de la récompense
     * @param merchant marchand proposant la récompense
     * @param name nom de la récompense
     * @param description description de la récompense
     * @param costPoints coût de la récompense en points
     * @param quantityAvailable quantité disponible, ou null si la quantité est illimitée
     * @param validFrom date de début de validité, ou null
     * @param validTo date de fin de validité, ou null
     * @param active indique si la récompense est active
     * @param createdAt date de création de la récompense
     */
    public Reward(
            UUID id,
            Merchant merchant,
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
        this.merchant = merchant;
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

    public Merchant getMerchant() {
        return merchant;
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

    /**

     * Vérifie si la récompense peut être utilisée à une date donnée.
     *
     * <p>La récompense est disponible uniquement si elle est active,
     * située dans sa période de validité et dispose encore d'une quantité
     * disponible lorsqu'une quantité limitée est définie.</p>
     *
     * @param dateTime date à laquelle vérifier la disponibilité
     * @return true si la récompense est disponible, sinon false
     */
    public boolean isAvailableAt(LocalDateTime dateTime) {

        if (!active) {
            return false;
        }

        if (validFrom != null && dateTime.isBefore(validFrom)) {
            return false;
        }

        if (validTo != null && dateTime.isAfter(validTo)) {
            return false;
        }

        return quantityAvailable == null || quantityAvailable > 0;
    }
}
