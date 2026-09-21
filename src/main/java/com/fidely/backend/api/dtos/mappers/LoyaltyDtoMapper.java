package com.fidely.backend.api.dtos.mappers;

import com.fidely.backend.api.dtos.loyalties.LoyaltyResponse;
import com.fidely.backend.api.dtos.loyalties.LoyaltyTransactionResponse;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de convertir les modèles de fidélité en DTO de réponse.
 */
@Component
public class LoyaltyDtoMapper {

    /**
     * Convertit un programme de fidélité en DTO de réponse.
     *
     * @param loyalty programme de fidélité à convertir
     * @return DTO correspondant au programme de fidélité
     */
    public LoyaltyResponse toResponse(Loyalty loyalty) {
        return new LoyaltyResponse(
                loyalty.getId(),
                loyalty.getCustomerId(),
                loyalty.getMerchantId(),
                loyalty.getPointsBalance()
        );
    }

    /**
     * Convertit une transaction de fidélité en DTO de réponse.
     *
     * @param transaction transaction à convertir
     * @return DTO correspondant à la transaction
     */
    public LoyaltyTransactionResponse toResponse(
            LoyaltyTransaction transaction
    ) {
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