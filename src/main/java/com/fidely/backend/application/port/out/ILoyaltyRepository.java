package com.fidely.backend.application.port.out;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant l'accès aux programmes de fidélité
 * et à leurs transactions de points.
 */
public interface ILoyaltyRepository {

    // Loyalty

    Optional<Loyalty> findById(UUID id);

    Optional<Loyalty> findByCustomerIdAndMerchantId(
            UUID customerId,
            UUID merchantId
    );

    List<Loyalty> findByCustomerId(UUID customerId);

    List<Loyalty> findByMerchantId(UUID merchantId);

    Loyalty save(Loyalty loyalty);

    void deleteById(UUID id);

    // Loyalty transactions

    LoyaltyTransaction saveTransaction(
            LoyaltyTransaction transaction
    );

    List<LoyaltyTransaction> findTransactionsByLoyaltyId(
            UUID loyaltyId
    );

    List<LoyaltyTransaction> findTransactionsByTicketId(
            UUID ticketId
    );
}
