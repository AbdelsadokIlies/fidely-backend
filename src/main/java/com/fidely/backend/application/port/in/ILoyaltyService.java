package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.tickets.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrée permettant de gérer les programmes de fidélité
 * et leurs transactions de points.
 */
public interface ILoyaltyService {

    /**
     * Crée un nouveau programme de fidélité pour un client
     * auprès d'un marchand.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return le programme de fidélité créé
     */
    Loyalty createLoyalty(
            UUID customerId,
            UUID merchantId
    );

    /**
     * Recherche un programme de fidélité par son identifiant.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @return le programme de fidélité s'il existe
     */
    Optional<Loyalty> getLoyaltyById(UUID loyaltyId);

    /**
     * Recherche le programme de fidélité d'un client chez un marchand.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return le programme de fidélité s'il existe
     */
    Optional<Loyalty> getLoyaltyByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    );

    /**
     * Récupère les programmes de fidélité d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des programmes de fidélité du client
     */
    List<Loyalty> getLoyaltiesByCustomer(UUID customerId);

    /**
     * Récupère les programmes de fidélité d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des programmes de fidélité du marchand
     */
    List<Loyalty> getLoyaltiesByMerchant(UUID merchantId);

    /**
     * Traite un ticket de caisse afin d'attribuer automatiquement
     * les points de fidélité correspondants.
     *
     * <p>Le ticket a préalablement été construit à partir des données
     * extraites par OCR. Cette opération vérifie le ticket, détermine
     * la règle de points applicable, calcule les points, met à jour
     * la fidélité et persiste la transaction.</p>
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param ticket ticket de caisse à traiter
     * @return la fidélité mise à jour après attribution des points
     */
    Loyalty addPointsFromTicket(
            UUID loyaltyId,
            Ticket ticket
    );

    /**
     * Récupère les transactions d'un programme de fidélité.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @return liste des transactions
     */
    List<LoyaltyTransaction> getTransactionsByLoyalty(
            UUID loyaltyId
    );

    /**
     * Récupère les transactions associées à un ticket.
     *
     * @param ticketId identifiant du ticket
     * @return liste des transactions associées au ticket
     */
    List<LoyaltyTransaction> getTransactionsByTicket(
            UUID ticketId
    );

    /**
     * Supprime un programme de fidélité.
     *
     * @param loyaltyId identifiant du programme de fidélité
     */
    void deleteLoyalty(UUID loyaltyId);
}