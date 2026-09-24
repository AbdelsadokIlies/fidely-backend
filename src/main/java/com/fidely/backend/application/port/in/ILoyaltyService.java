package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.tickets.Ticket;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**

 * Port d'entrée permettant de gérer les programmes de fidélité
 * et leurs transactions de points.
 */
public interface ILoyaltyService {

    /**

     * Crée une nouvelle fidélité pour un client auprès d'un marchand.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return fidélité créée
     */
    Loyalty createLoyalty(UUID customerId, UUID merchantId);

    /**

     * Recherche une fidélité par son identifiant.
     *
     * @param loyaltyId identifiant de la fidélité
     * @return fidélité si elle existe
     */
    Optional<Loyalty> getLoyaltyById(UUID loyaltyId);

    /**

     * Recherche la fidélité d'un client auprès d'un marchand.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return fidélité si elle existe
     */
    Optional<Loyalty> getLoyaltyByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    );

    /**

     * Recherche toutes les fidélités d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des fidélités du client
     */
    List<Loyalty> getLoyaltiesByCustomer(UUID customerId);

    /**

     * Recherche toutes les fidélités d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des fidélités du marchand
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
    Loyalty addPointsFromTicket(UUID loyaltyId, Ticket ticket);

    /**

     * Enregistre manuellement une transaction de fidélité.
     *
     * <p>Cette opération est utilisée lorsqu'un marchand souhaite
     * attribuer des points à un client sans utiliser de ticket OCR.
     * Aucun ticket n'est créé pour cette opération.</p>
     *
     * @param loyaltyId identifiant de la fidélité
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @param amount montant de la transaction
     * @return la fidélité mise à jour après attribution des points
     */
    Loyalty addPointsManually(
            UUID loyaltyId,
            UUID merchantId,
            UUID customerId,
            BigDecimal amount
    );

    /**

     * Recherche les transactions d'une fidélité.
     *
     * @param loyaltyId identifiant de la fidélité
     * @return transactions associées
     */
    List<LoyaltyTransaction> getTransactionsByLoyalty(UUID loyaltyId);

    /**

     * Recherche la transaction associée à un ticket.
     *
     * @param ticketId identifiant du ticket
     * @return transactions associées au ticket
     */
    List<LoyaltyTransaction> getTransactionsByTicket(UUID ticketId);

    /**

     * Supprime une fidélité.
     *
     * @param loyaltyId identifiant de la fidélité
     */
    void deleteLoyalty(UUID loyaltyId);
}