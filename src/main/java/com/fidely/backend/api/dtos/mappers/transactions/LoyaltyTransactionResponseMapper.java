package com.fidely.backend.api.dtos.mappers.transactions;

import com.fidely.backend.api.dtos.models.transactions.LoyaltyTransactionResponse;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import org.springframework.stereotype.Component;

/**

 * Mapper permettant de transformer une transaction de fidélité
 * du domaine en réponse API.
 *
 * <p>Ce mapper ne contient aucune logique métier et ne réalise
 * aucun accès à la persistance.</p>
 */
@Component
public class LoyaltyTransactionResponseMapper {

    /**

     * Transforme une transaction de fidélité en réponse API.
     *
     * @param transaction transaction métier à transformer
     * @return réponse API correspondante
     * @throws IllegalArgumentException si la transaction est null
     */
    public LoyaltyTransactionResponse toResponse(
            LoyaltyTransaction transaction
    ) {
        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Loyalty transaction cannot be null"
            );
        }

        return new LoyaltyTransactionResponse(
                transaction.getId(),
                transaction.getLoyaltyId(),
                transaction.getTicketId(),
                transaction.getPoints(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
