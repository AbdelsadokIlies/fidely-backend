package com.fidely.backend.repository.entities.loyalties.Rewards;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité JPA représentant une règle d'attribution de points.
 */
@Entity
@Table(name = "point_rules")
public class PointRuleEntity {

    @Id
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(
            name = "points_per_currency_unit",
            nullable = false,
            precision = 10,
            scale = 4
    )
    private BigDecimal pointsPerCurrencyUnit;

    @Column(name = "rounding_method", nullable = false)
    private short roundingMethod;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Constructeur utilisé par JPA.
     */
    protected PointRuleEntity() {
        // Required by JPA
    }

    /**
     * Crée une nouvelle entité de règle d'attribution de points.
     *
     * @param id identifiant de la règle
     * @param merchantId identifiant du marchand
     * @param pointsPerCurrencyUnit nombre de points par unité monétaire
     * @param roundingMethod méthode d'arrondi utilisée
     * @param active indique si la règle est active
     * @param validFrom date de début de validité
     * @param validTo date de fin de validité
     * @param createdAt date de création de la règle
     */
    public PointRuleEntity(
            UUID id,
            UUID merchantId,
            BigDecimal pointsPerCurrencyUnit,
            short roundingMethod,
            boolean active,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.merchantId = merchantId;
        this.pointsPerCurrencyUnit = pointsPerCurrencyUnit;
        this.roundingMethod = roundingMethod;
        this.active = active;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public BigDecimal getPointsPerCurrencyUnit() {
        return pointsPerCurrencyUnit;
    }

    public short getRoundingMethod() {
        return roundingMethod;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}