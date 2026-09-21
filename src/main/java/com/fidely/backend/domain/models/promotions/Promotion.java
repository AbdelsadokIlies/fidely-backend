package com.fidely.backend.domain.models.promotions;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente une promotion proposée par un marchand.
 *
 * <p>Une promotion définit une période de validité et une règle permettant
 * de déterminer ses conditions d'application.</p>
 */
public class Promotion {

    private final UUID id;
    private final UUID merchantId;
    private final String name;
    private final String description;
    private final String ruleJson;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final boolean active;
    private final LocalDateTime createdAt;

    /**
     * Crée une nouvelle promotion.
     *
     * @param id identifiant de la promotion
     * @param merchantId id du marchand proposant la promotion
     * @param name nom de la promotion
     * @param description description de la promotion
     * @param ruleJson règle de la promotion au format JSON
     * @param startDate date de début de la promotion
     * @param endDate date de fin de la promotion
     * @param active indique si la promotion est active
     * @param createdAt date de création de la promotion
     */
    public Promotion(
            UUID id,
            UUID merchantId,
            String name,
            String description,
            String ruleJson,
            LocalDateTime startDate,
            LocalDateTime endDate,
            boolean active,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.name = name;
        this.description = description;
        this.ruleJson = ruleJson;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getRuleJson() {
        return ruleJson;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}