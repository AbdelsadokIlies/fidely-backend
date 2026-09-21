package com.fidely.backend.api.dtos.mappers;

import com.fidely.backend.api.dtos.loyalties.LoyaltyResponse;
import com.fidely.backend.api.dtos.loyalties.LoyaltyTransactionResponse;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyDtoMapper {

    public LoyaltyResponse toResponse(Loyalty loyalty) {
        return new LoyaltyResponse(
                loyalty.getId(),
                loyalty.getCustomerId(),
                loyalty.getMerchantId(),
                loyalty.getPointsBalance()
        );
    }

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