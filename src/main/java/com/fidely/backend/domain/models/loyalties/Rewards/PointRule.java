package com.fidely.backend.domain.models.loyalties.Rewards;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Définit une règle permettant de calculer le nombre de points de fidélité
 * accordés en fonction d'un montant dépensé.
 *
 * <p>Une règle peut être active pendant une période donnée et utilise
 * une méthode d'arrondi pour déterminer le nombre final de points.</p>
 */
public class PointRule {

    private final UUID id;
    private final BigDecimal pointsPerCurrencyUnit;
    private final RoundingMethod roundingMethod;
    private final boolean active;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;
    private final LocalDateTime createdAt;

    /**

     * Crée une nouvelle règle de calcul de points.
     *
     * @param id identifiant de la règle
     * @param pointsPerCurrencyUnit nombre de points attribués par unité monétaire
     * @param roundingMethod méthode d'arrondi utilisée pour le calcul
     * @param active indique si la règle est active
     * @param validFrom date à partir de laquelle la règle est valide
     * @param validTo date jusqu'à laquelle la règle est valide, ou null sans date de fin
     * @param createdAt date de création de la règle
     * @throws IllegalArgumentException si le nombre de points par unité monétaire
     * est nul ou inférieur à zéro, si la méthode d'arrondi est nulle,
     * si la date de début est nulle ou si la date de début est postérieure
     * à la date de fin
     */
    public PointRule(
            UUID id,
            BigDecimal pointsPerCurrencyUnit,
            RoundingMethod roundingMethod,
            boolean active,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime createdAt
    ) {
        if (pointsPerCurrencyUnit == null ||
                pointsPerCurrencyUnit.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Points per currency unit must be greater than zero"
            );
        }

        if (roundingMethod == null) {
            throw new IllegalArgumentException(
                    "Rounding method cannot be null"
            );
        }

        if (validFrom == null) {
            throw new IllegalArgumentException(
                    "Valid from cannot be null"
            );
        }

        if (validTo != null && validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException(
                    "Valid from cannot be after valid to"
            );
        }

        this.id = id;
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

    public BigDecimal getPointsPerCurrencyUnit() {
        return pointsPerCurrencyUnit;
    }

    public RoundingMethod getRoundingMethod() {
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

    /**

     * Calcule le nombre de points accordés pour un montant donné.
     *
     * <p>Le résultat est arrondi selon la méthode définie par la règle :
     * arrondi inférieur, arrondi classique ou arrondi supérieur.</p>
     *
     * @param amount montant utilisé pour calculer les points
     * @return nombre de points calculés
     * @throws IllegalArgumentException si le montant est null ou négatif
     */
    public int calculatePoints(BigDecimal amount) {

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Amount cannot be null"
            );
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Amount cannot be negative"
            );
        }

        BigDecimal rawPoints =
                amount.multiply(pointsPerCurrencyUnit);

        return switch (roundingMethod) {
            case FLOOR -> rawPoints.setScale(0, RoundingMode.FLOOR).intValue();
            case ROUND -> rawPoints.setScale(0, RoundingMode.HALF_UP).intValue();
            case CEIL -> rawPoints.setScale(0, RoundingMode.CEILING).intValue();
        };
    }

    /**

     * Vérifie si la règle est valide à une date donnée.
     *
     * <p>La règle est valide uniquement si elle est active et que la date
     * se situe dans sa période de validité.</p>
     *
     * @param date date à vérifier
     * @return true si la règle est valide à cette date, sinon false
     * @throws IllegalArgumentException si la date est null
     */
    public boolean isValidAt(LocalDateTime date) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Date cannot be null"
            );
        }

        if (!active) {
            return false;
        }

        if (date.isBefore(validFrom)) {
            return false;
        }

        return validTo == null || !date.isAfter(validTo);
    }
}
