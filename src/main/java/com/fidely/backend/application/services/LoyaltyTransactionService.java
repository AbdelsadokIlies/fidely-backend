package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Service responsable de la gestion des transactions de fidélité.
 *
 * <p>Ce service orchestre l'ajout de points à partir d'un ticket
 * ou à partir d'une transaction enregistrée manuellement par
 * un marchand.</p>
 *
 * <p>Dans le cas d'une transaction issue d'un ticket, le ticket
 * est vérifié, persisté puis associé à la transaction de fidélité.
 * Dans le cas d'une transaction manuelle, aucun ticket n'est créé
 * et la transaction de fidélité ne référence donc aucun ticket.</p>
 */
@Service
public class LoyaltyTransactionService {

    private final ILoyaltyRepository loyaltyRepository;
    private final ITicketService ticketService;
    private final IPointRuleService pointRuleService;

    /**

     * Construit le service de transactions de fidélité.
     *
     * @param loyaltyRepository repository des fidélités et transactions
     * @param ticketService service de gestion des tickets
     * @param pointRuleService service de gestion des règles de points
     */
    public LoyaltyTransactionService(
            ILoyaltyRepository loyaltyRepository,
            ITicketService ticketService,
            IPointRuleService pointRuleService
    ) {
        this.loyaltyRepository = loyaltyRepository;
        this.ticketService = ticketService;
        this.pointRuleService = pointRuleService;
    }

    /**

     * Ajoute les points correspondant à un ticket.
     *
     * <p>Le ticket fourni doit être un ticket métier temporaire issu de
     * l'étape d'extraction OCR. Cette méthode vérifie qu'il n'a pas déjà
     * été utilisé, vérifie la règle de points applicable, calcule les
     * points, met à jour la fidélité, persiste le ticket, crée la
     * transaction puis valide le ticket.</p>
     *
     * @param loyaltyId identifiant de la fidélité du client chez le marchand
     * @param ticket ticket à traiter
     * @return fidélité mise à jour
     * @throws IllegalArgumentException si la fidélité est introuvable,
     * si le ticket n'appartient pas au marchand ou au client
     * @throws IllegalStateException si le ticket est déjà utilisé,
     * si aucune règle de points valide n'existe ou si le ticket
     * ne génère aucun point
     */
    @Transactional
    public Loyalty addPointsFromTicket(
            UUID loyaltyId,
            Ticket ticket
    ) {
        Loyalty loyalty = loyaltyRepository.findById(loyaltyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Loyalty introuvable : " + loyaltyId
                ));

        if (!loyalty.getMerchantId().equals(ticket.getMerchantId())) {
            throw new IllegalArgumentException(
                    "Le ticket n'appartient pas au marchand de la fidélité."
            );
        }

        if (!loyalty.getCustomerId().equals(ticket.getCustomerId())) {
            throw new IllegalArgumentException(
                    "Le ticket n'appartient pas au client de la fidélité."
            );
        }

        if (ticketService.existsByFingerprintHash(
                ticket.getFingerprintHash()
        )) {
            throw new IllegalStateException(
                    "Ce ticket a déjà été utilisé."
            );
        }

        LocalDateTime ticketDateTime = LocalDateTime.of(
                ticket.getTicketDate(),
                ticket.getTicketTime()
        );

        PointRule pointRule = pointRuleService.getValidPointRule(
                ticket.getMerchantId(),
                ticketDateTime
        ).orElseThrow(() -> new IllegalStateException(
                "Aucune règle de points valide pour le marchand : "
                        + ticket.getMerchantId()
        ));

        int points = pointRule.calculatePoints(ticket.getAmount());

        if (points <= 0) {
            throw new IllegalStateException(
                    "Le ticket ne génère aucun point : " + ticket.getId()
            );
        }

        loyalty.addPoints(points);
        loyaltyRepository.save(loyalty);

        Ticket persistedTicket = ticketService.createTicket(ticket);

        LoyaltyTransaction transaction = new LoyaltyTransaction(
                UUID.randomUUID(),
                loyalty.getId(),
                persistedTicket.getId(),
                points,
                "Points gagnés sur le ticket "
                        + persistedTicket.getTicketNumber(),
                LocalDateTime.now()
        );

        loyaltyRepository.saveTransaction(transaction);

        ticketService.validateTicket(persistedTicket.getId());

        return loyalty;
    }

    /**

     * Ajoute manuellement des points à une fidélité.
     *
     * <p>Cette opération est utilisée lorsqu'un marchand enregistre
     * directement une transaction pour un client identifié. Aucun
     * ticket de caisse n'est créé et la transaction de fidélité
     * enregistrée ne référence donc aucun ticket.</p>
     *
     * <p>La règle de points applicable est déterminée à partir
     * de la date et de l'heure actuelles.</p>
     *
     * @param loyaltyId identifiant de la fidélité du client
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @param amount montant de la transaction
     * @return fidélité mise à jour
     * @throws IllegalArgumentException si la fidélité est introuvable,
     * si elle n'appartient pas au marchand ou au client fourni,
     * ou si le montant est invalide
     * @throws IllegalStateException si aucune règle de points valide
     * n'existe ou si la transaction ne génère aucun point
     */
    @Transactional
    public Loyalty addPointsManually(
            UUID loyaltyId,
            UUID merchantId,
            UUID customerId,
            BigDecimal amount
    ) {
        if (merchantId == null) {
            throw new IllegalArgumentException(
                    "Merchant cannot be null"
            );
        }

        if (customerId == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Amount cannot be null"
            );
        }

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        Loyalty loyalty = loyaltyRepository.findById(loyaltyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Loyalty introuvable : " + loyaltyId
                ));

        if (!loyalty.getMerchantId().equals(merchantId)) {
            throw new IllegalArgumentException(
                    "La fidélité n'appartient pas au marchand fourni."
            );
        }

        if (!loyalty.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "La fidélité n'appartient pas au client fourni."
            );
        }

        LocalDateTime transactionDateTime = LocalDateTime.now();

        PointRule pointRule = pointRuleService.getValidPointRule(
                merchantId,
                transactionDateTime
        ).orElseThrow(() -> new IllegalStateException(
                "Aucune règle de points valide pour le marchand : "
                        + merchantId
        ));

        int points = pointRule.calculatePoints(amount);

        if (points <= 0) {
            throw new IllegalStateException(
                    "La transaction ne génère aucun point."
            );
        }

        loyalty.addPoints(points);
        loyaltyRepository.save(loyalty);

        LoyaltyTransaction transaction = new LoyaltyTransaction(
                UUID.randomUUID(),
                loyalty.getId(),
                null,
                points,
                "Points gagnés sur une transaction manuelle",
                transactionDateTime
        );

        loyaltyRepository.saveTransaction(transaction);

        return loyalty;
    }
}