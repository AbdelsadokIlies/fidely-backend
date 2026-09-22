package com.fidely.backend.api.dtos.mappers.transactions;

import com.fidely.backend.api.dtos.models.transactions.CreateTransactionResponse;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de transformer une fidélité métier
 * en réponse API après la création d'une transaction.
 *
 * <p>Ce mapper ne contient aucune logique métier et ne réalise
 * aucun accès à la persistance.</p>
 */
@Component
public class CreateTransactionResponseMapper {

    /**
     * Transforme une fidélité en réponse API.
     *
     * @param loyalty fidélité mise à jour
     * @return réponse API contenant le nouveau solde de points
     * @throws IllegalArgumentException si la fidélité est null
     */
    public CreateTransactionResponse toResponse(Loyalty loyalty) {
        if (loyalty == null) {
            throw new IllegalArgumentException(
                    "Loyalty cannot be null"
            );
        }

        return new CreateTransactionResponse(
                loyalty.getId(),
                loyalty.getCustomerId(),
                loyalty.getMerchantId(),
                loyalty.getPointsBalance(),
                loyalty.getUpdatedAt()
        );
    }
}