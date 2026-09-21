package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrée permettant de gérer les programmes de fidélité
 * et leurs transactions de points.
 */
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

    /**
     * Traite une image de ticket de caisse et attribue automatiquement
     * les points de fidélité correspondants.
     *
     * <p>Le traitement comprend l'analyse OCR du ticket, la création
     * du ticket, la détermination de la règle de points applicable,
     * le calcul des points et leur attribution à la fidélité.</p>
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param image image du ticket de caisse
     * @return la fidélité mise à jour après attribution des points
     */
    Loyalty addPointsFromTicket(
            UUID loyaltyId,
            byte[] image
    );

    List<LoyaltyTransaction> getTransactionsByLoyalty(
            UUID loyaltyId
    );

    List<LoyaltyTransaction> getTransactionsByTicket(
            UUID ticketId
    );

    void deleteLoyalty(UUID loyaltyId);
}