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
 * d'une opération complète d'attribution de points.
 *
 * <p>Ce service orchestre les différentes opérations nécessaires
 * au traitement d'un ticket : création du ticket à partir de l'image,
 * vérification de son utilisation, détermination de la règle de points,
 * calcul des points, mise à jour de la fidélité, création de la
 * transaction de fidélité et validation du ticket.</p>
 *
 * <p>L'ensemble de l'opération est exécuté dans une transaction
 * afin de garantir qu'une attribution partiellement effectuée
 * ne puisse pas être persistée.</p>
 */
@Service
public class LoyaltyTransactionService {

    private final ILoyaltyRepository loyaltyRepository;
    private final ITicketService ticketService;
    private final IPointRuleService pointRuleService;

    /**
     * Construit le service d'exécution transactionnelle
     * des opérations de fidélité.
     *
     * @param loyaltyRepository repository des programmes de fidélité
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
     * Exécute une tentative complète d'attribution des points
     * à partir d'une image de ticket.
     *
     * <p>Le ticket est d'abord analysé par le service OCR via
     * le {@link ITicketService}. Une fois créé, les données du ticket
     * permettent de déterminer la règle de points applicable et
     * de calculer le nombre de points à attribuer.</p>
     *
     * <p>Si une erreur survient au cours du traitement, l'ensemble
     * de la transaction est rollbacké.</p>
     *
     * @param loyaltyId identifiant du programme de fidélité
     * @param image image du ticket de caisse
     * @return le programme de fidélité mis à jour
     */
    @Transactional
    public Loyalty addPointsFromTicket(
            UUID loyaltyId,
            byte[] image
    ) {
        Loyalty loyalty = loyaltyRepository.findById(loyaltyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Loyalty introuvable : " + loyaltyId
                        )
                );

        Ticket ticket = ticketService.createTicketFromOcr(
                image,
                loyalty.getMerchantId(),
                loyalty.getCustomerId()
        );

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

        int points = pointRule.calculatePoints(
                ticket.getAmount()
        );

        if (points <= 0) {
            throw new IllegalStateException(
                    "Le ticket ne génère aucun point : "
                            + ticket.getId()
            );
        }

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

        ticketService.validateTicket(ticket.getId());

        return loyalty;
    }
}