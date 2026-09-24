package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**

 * Service applicatif responsable de la gestion des programmes
 * de fidélité.
 *
 * <p>Ce service constitue le point d'entrée applicatif pour les
 * opérations liées aux programmes de fidélité, notamment l'attribution
 * de points à partir d'un ticket de caisse ou l'enregistrement manuel
 * d'une transaction.</p>
 *
 * <p>Les opérations transactionnelles d'attribution de points sont
 * déléguées à {@link LoyaltyTransactionService}. Ce service gère
 * également les éventuels conflits d'optimistic locking.</p>
 */
@Service
public class LoyaltyService implements ILoyaltyService {

    /**

     * Nombre maximal de tentatives lors d'un conflit
     * d'optimistic locking.
     */
    private static final int MAX_RETRIES = 3;

    private final ILoyaltyRepository loyaltyRepository;
    private final LoyaltyTransactionService loyaltyTransactionService;

    /**

     * Construit le service de gestion des fidélités.
     *
     * @param loyaltyRepository repository des programmes de fidélité
     * @param loyaltyTransactionService service responsable de l'exécution
     * ```
    transactionnelle des opérations
    ```
     * ```
    de fidélité
    ```

     */
    public LoyaltyService(
            ILoyaltyRepository loyaltyRepository,
            LoyaltyTransactionService loyaltyTransactionService
    ) {
        this.loyaltyRepository = loyaltyRepository;
        this.loyaltyTransactionService = loyaltyTransactionService;
    }

    /**

     * Crée un nouveau programme de fidélité.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return le programme de fidélité créé
     */
    @Override
    public Loyalty createLoyalty(
            UUID customerId,
            UUID merchantId
    ) {
        LocalDateTime now = LocalDateTime.now();

        Loyalty loyalty = new Loyalty(
                UUID.randomUUID(),
                customerId,
                merchantId,
                0,
                now,
                now
        );

        return loyaltyRepository.save(loyalty);
    }

    /**

     * Recherche un programme de fidélité par son identifiant.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @return le programme de fidélité s'il existe, sinon un Optional vide
     */

    @Override
    public Optional<Loyalty> getLoyaltyById(UUID loyaltyId) {
        return loyaltyRepository.findById(loyaltyId);
    }

    /**

     * Recherche le programme de fidélité d'un client chez un marchand.
     *
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return le programme de fidélité s'il existe, sinon un Optional vide
     */
    @Override
    public Optional<Loyalty> getLoyaltyByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    ) {
        return loyaltyRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        );
    }

    /**

     * Récupère les programmes de fidélité d'un client.
     *
     * @param customerId identifiant du client
     * @return liste des programmes de fidélité du client
     */
    @Override
    public List<Loyalty> getLoyaltiesByCustomer(UUID customerId) {
        return loyaltyRepository.findByCustomerId(customerId);
    }

    /**

     * Récupère les programmes de fidélité d'un marchand.
     *
     * @param merchantId identifiant du marchand
     * @return liste des programmes de fidélité du marchand
     */
    @Override
    public List<Loyalty> getLoyaltiesByMerchant(UUID merchantId) {
        return loyaltyRepository.findByMerchantId(merchantId);
    }

    /**

     * Traite un ticket de caisse afin d'attribuer automatiquement
     * les points de fidélité correspondants.
     *
     * <p>Le ticket doit avoir été préalablement construit par le flux
     * applicatif. Cette méthode ne réalise pas l'extraction OCR.</p>
     *
     * <p>En cas de conflit d'optimistic locking, toute la tentative
     * transactionnelle est rejouée depuis le début.</p>
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param ticket ticket de caisse à traiter
     * @return le programme de fidélité mis à jour
     */
    @Override
    public Loyalty addPointsFromTicket(
            UUID loyaltyId,
            Ticket ticket
    ) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return loyaltyTransactionService.addPointsFromTicket(
                        loyaltyId,
                        ticket
                );
            } catch (OptimisticLockingFailureException exception) {
                if (attempt == MAX_RETRIES) {
                    throw exception;
                }
            }
        }

        throw new IllegalStateException(
                "Impossible de traiter le ticket après "
                        + MAX_RETRIES
                        + " tentatives."
        );
    }

    /**

     * Enregistre manuellement une transaction de fidélité.
     *
     * <p>Cette méthode délègue l'opération au service responsable
     * des transactions de fidélité. Aucun ticket n'est créé pour
     * une transaction manuelle.</p>
     *
     * <p>En cas de conflit d'optimistic locking, toute la tentative
     * transactionnelle est rejouée depuis le début.</p>
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @param amount montant de la transaction
     * @return le programme de fidélité mis à jour
     */
    @Override
    public Loyalty addPointsManually(
            UUID loyaltyId,
            UUID merchantId,
            UUID customerId,
            BigDecimal amount
    ) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        amount
                );
            } catch (OptimisticLockingFailureException exception) {
                if (attempt == MAX_RETRIES) {
                    throw exception;
                }
            }
        }

        throw new IllegalStateException(
                "Impossible d'enregistrer la transaction manuelle après "
                        + MAX_RETRIES
                        + " tentatives."
        );
    }

    /**

     * Récupère les transactions d'un programme de fidélité.
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @return liste des transactions
     */
    @Override
    public List<LoyaltyTransaction> getTransactionsByLoyalty(
            UUID loyaltyId
    ) {
        return loyaltyRepository.findTransactionsByLoyaltyId(loyaltyId);
    }

    /**

     * Récupère les transactions associées à un ticket.
     *
     * @param ticketId identifiant du ticket
     * @return liste des transactions associées au ticket
     */
    @Override
    public List<LoyaltyTransaction> getTransactionsByTicket(
            UUID ticketId
    ) {
        return loyaltyRepository.findTransactionsByTicketId(ticketId);
    }

    /**

     * Supprime un programme de fidélité.
     *
     * @param loyaltyId identifiant du programme de fidélité
     */
    @Override
    public void deleteLoyalty(UUID loyaltyId) {
        loyaltyRepository.deleteById(loyaltyId);
    }
}
