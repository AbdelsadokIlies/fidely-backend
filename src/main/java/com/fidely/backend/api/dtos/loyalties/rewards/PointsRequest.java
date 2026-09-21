package com.fidely.backend.api.dtos.loyalties.rewards;

import jakarta.validation.constraints.Positive;

/**
 * Requête contenant un nombre de points à ajouter ou retirer.
 *
 * @param points nombre de points concernés par l'opération
 */
public record PointsRequest(

        @Positive
        int points
) {
}