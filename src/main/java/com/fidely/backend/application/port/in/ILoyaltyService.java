package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ILoyaltyService {

    Loyalty createLoyalty(
            UUID customerId,
            UUID merchantId
    );

    Optional<Loyalty> getLoyaltyById(UUID loyaltyId);

    Optional<Loyalty> getLoyaltyByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    );

    List<Loyalty> getLoyaltiesByCustomer(UUID customerId);

    List<Loyalty> getLoyaltiesByMerchant(UUID merchantId);

    void addPoints(UUID loyaltyId, int points);

    void removePoints(UUID loyaltyId, int points);

    void addPointsFromTicket(UUID ticketId);

    List<LoyaltyTransaction> getTransactionsByLoyalty(
            UUID loyaltyId
    );

    List<LoyaltyTransaction> getTransactionsByTicket(
            UUID ticketId
    );

    void deleteLoyalty(UUID loyaltyId);
}