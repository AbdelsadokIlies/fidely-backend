package com.fidely.backend.api.dtos.models.loyalties.rewards;


import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Requête permettant de créer une nouvelle règle d'attribution de points.
 *
 * @param pointsPerCurrencyUnit nombre de points attribués par unité monétaire
 * @param roundingMethod méthode d'arrondi utilisée
 * @param active indique si la règle est active
 * @param validFrom date de début de validité
 * @param validTo date de fin de validité
 */
public record UpdatePointRuleRequest(
        BigDecimal pointsPerCurrencyUnit,
        RoundingMethod roundingMethod,
        boolean active,
        LocalDateTime validFrom,
        LocalDateTime validTo
) {
}