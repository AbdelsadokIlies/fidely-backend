package com.fidely.backend.application;

import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.tickets.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service responsable de l'exécution transactionnelle
 * d'une opération d'ajout de points à partir d'un ticket.
 *
 * <p>Chaque appel à {@link #addPointsFromTicket(UUID)} correspond
 * à une tentative complète et atomique.</p>
 */
@Service
public class LoyaltyTransactionService {

    private final ILoyaltyRepository loyaltyRepository;
    private final ITicketService ticketService;
    private final IPointRuleService pointRuleService;

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
     * Exécute une tentative complète d'attribution des points.
     *
     * <p>Si une erreur survient, l'ensemble de la transaction
     * est rollbacké.</p>
     *
     * @param ticketId identifiant du ticket
     */
    @Transactional
    public void addPointsFromTicket(UUID ticketId) {

        Ticket ticket = ticketService.getTicketById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket introuvable : " + ticketId
                        )
                );

        List<LoyaltyTransaction> existingTransactions =
                loyaltyRepository.findTransactionsByTicketId(ticketId);

        if (!existingTransactions.isEmpty()) {
            throw new IllegalStateException(
                    "Points already awarded for ticket : " + ticketId
            );
        }

        LocalDateTime ticketDateTime = LocalDateTime.of(
                ticket.getTicketDate(),
                ticket.getTicketTime()
        );

        PointRule pointRule =
                pointRuleService.getValidPointRule(
                        ticket.getMerchantId(),
                        ticketDateTime
                ).orElseThrow(() ->
                        new IllegalStateException(
                                "Aucune règle de points valide pour le marchand : "
                                        + ticket.getMerchantId()
                        )
                );

        int points =
                pointRule.calculatePoints(ticket.getAmount());

        if (points <= 0) {
            throw new IllegalStateException(
                    "Le ticket ne génère aucun point : " + ticketId
            );
        }

        Loyalty loyalty =
                loyaltyRepository
                        .findByCustomerIdAndMerchantId(
                                ticket.getCustomerId(),
                                ticket.getMerchantId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Loyalty introuvable pour le client : "
                                                + ticket.getCustomerId()
                                )
                        );

        loyalty.addPoints(points);

        loyaltyRepository.save(loyalty);

        LoyaltyTransaction transaction =
                new LoyaltyTransaction(
                        UUID.randomUUID(),
                        loyalty.getId(),
                        ticket.getId(),
                        points,
                        "Points gagnés sur le ticket "
                                + ticket.getTicketNumber(),
                        LocalDateTime.now()
                );

        loyaltyRepository.saveTransaction(transaction);

        ticketService.validateTicket(ticketId);
    }
}