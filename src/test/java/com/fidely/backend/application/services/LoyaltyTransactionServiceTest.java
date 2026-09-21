package com.fidely.backend.application.services;

import com.fidely.backend.application.LoyaltyTransactionService;
import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service responsable de l'exécution
 * transactionnelle d'une opération de fidélité à partir
 * d'une image de ticket.
 */
@ExtendWith(MockitoExtension.class)
class LoyaltyTransactionServiceTest {

    @Mock
    private ILoyaltyRepository loyaltyRepository;

    @Mock
    private ITicketService ticketService;

    @Mock
    private IPointRuleService pointRuleService;

    private LoyaltyTransactionService loyaltyTransactionService;

    /**
     * Initialise le service testé avant chaque test.
     */
    @BeforeEach
    void setUp() {
        loyaltyTransactionService =
                new LoyaltyTransactionService(
                        loyaltyRepository,
                        ticketService,
                        pointRuleService
                );
    }

    /**
     * Vérifie qu'une fidélité inexistante empêche
     * le traitement du ticket.
     */
    @Test
    void shouldRejectWhenLoyaltyDoesNotExist() {
        UUID loyaltyId = UUID.randomUUID();

        byte[] image = "fake-image".getBytes();

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsFromTicket(
                        loyaltyId,
                        image
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Loyalty introuvable : " + loyaltyId
                );

        verifyNoInteractions(ticketService);
        verifyNoInteractions(pointRuleService);

        verify(loyaltyRepository, never())
                .save(any());

        verify(loyaltyRepository, never())
                .saveTransaction(any());
    }

    /**
     * Vérifie qu'une absence de règle de points valide
     * empêche l'attribution des points.
     */
    @Test
    void shouldRejectWhenNoValidPointRuleExists() {
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        byte[] image = "fake-image".getBytes();

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("50.00")
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(ticketService.createTicketFromOcr(
                eq(image),
                eq(merchantId),
                eq(customerId)
        )).thenReturn(ticket);

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsFromTicket(
                        loyaltyId,
                        image
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Aucune règle de points valide pour le marchand : "
                                + merchantId
                );

        verify(loyaltyRepository, never())
                .save(any());

        verify(loyaltyRepository, never())
                .saveTransaction(any());

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

    /**
     * Vérifie qu'un ticket générant zéro point est rejeté
     * avant toute modification de la fidélité.
     */
    @Test
    void shouldRejectWhenCalculatedPointsAreZero() {
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        byte[] image = "fake-image".getBytes();

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("0.01")
        );

        PointRule pointRule = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("0.0001"),
                RoundingMethod.FLOOR,
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(ticketService.createTicketFromOcr(
                eq(image),
                eq(merchantId),
                eq(customerId)
        )).thenReturn(ticket);

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(pointRule));

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsFromTicket(
                        loyaltyId,
                        image
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Le ticket ne génère aucun point : "
                                + ticketId
                );

        assertThat(loyalty.getPointsBalance())
                .isZero();

        verify(loyaltyRepository, never())
                .save(any());

        verify(loyaltyRepository, never())
                .saveTransaction(any());

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

    /**
     * Vérifie qu'un ticket n'est pas validé si la transaction
     * de fidélité ne peut pas être sauvegardée.
     */
    @Test
    void shouldNotValidateTicketWhenTransactionCannotBeSaved() {
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        byte[] image = "fake-image".getBytes();

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("112.00")
        );

        PointRule pointRule = createPointRule();

        LocalDateTime ticketDateTime = LocalDateTime.of(
                ticket.getTicketDate(),
                ticket.getTicketTime()
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(ticketService.createTicketFromOcr(
                image,
                merchantId,
                customerId
        )).thenReturn(ticket);

        when(pointRuleService.getValidPointRule(
                merchantId,
                ticketDateTime
        )).thenReturn(Optional.of(pointRule));

        doThrow(new IllegalStateException(
                "Erreur lors de la sauvegarde"
        ))
                .when(loyaltyRepository)
                .saveTransaction(any(LoyaltyTransaction.class));

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsFromTicket(
                        loyaltyId,
                        image
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Erreur lors de la sauvegarde");

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        verify(loyaltyRepository)
                .save(loyalty);

        verify(loyaltyRepository)
                .saveTransaction(any(LoyaltyTransaction.class));

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

    /**
     * Vérifie qu'un ticket issu de l'OCR permet d'ajouter
     * les points, de créer une transaction et de valider
     * le ticket.
     */
    @Test
    void shouldAddPointsCreateTransactionAndValidateTicket() {
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        byte[] image = "fake-image".getBytes();

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("112.00")
        );

        PointRule pointRule = createPointRule();

        LocalDateTime ticketDateTime = LocalDateTime.of(
                ticket.getTicketDate(),
                ticket.getTicketTime()
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(ticketService.createTicketFromOcr(
                image,
                merchantId,
                customerId
        )).thenReturn(ticket);

        when(pointRuleService.getValidPointRule(
                merchantId,
                ticketDateTime
        )).thenReturn(Optional.of(pointRule));

        when(loyaltyRepository.save(loyalty))
                .thenReturn(loyalty);

        loyaltyTransactionService.addPointsFromTicket(
                loyaltyId,
                image
        );

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        verify(ticketService)
                .createTicketFromOcr(
                        image,
                        merchantId,
                        customerId
                );

        verify(loyaltyRepository)
                .save(loyalty);

        verify(loyaltyRepository)
                .saveTransaction(
                        any(LoyaltyTransaction.class)
                );

        verify(ticketService)
                .validateTicket(ticketId);
    }

    /**
     * Crée une fidélité de test.
     *
     * @param id identifiant de la fidélité
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return fidélité de test
     */
    private Loyalty createLoyalty(
            UUID id,
            UUID customerId,
            UUID merchantId
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new Loyalty(
                id,
                customerId,
                merchantId,
                0,
                now,
                now
        );
    }

    /**
     * Crée une règle de points de test.
     *
     * @return règle de points valide
     */
    private PointRule createPointRule() {
        LocalDateTime validFrom =
                LocalDateTime.of(
                        2026,
                        1,
                        1,
                        0,
                        0
                );

        return new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                LocalDateTime.of(
                        2026,
                        12,
                        31,
                        23,
                        59
                ),
                validFrom
        );
    }

    /**
     * Crée une transaction de fidélité de test.
     *
     * @param id identifiant de la transaction
     * @param loyaltyId identifiant de la fidélité
     * @param ticketId identifiant du ticket
     * @param points nombre de points
     * @return transaction de test
     */
    private LoyaltyTransaction createTransaction(
            UUID id,
            UUID loyaltyId,
            UUID ticketId,
            int points
    ) {
        return new LoyaltyTransaction(
                id,
                loyaltyId,
                ticketId,
                points,
                "Purchase",
                LocalDateTime.now()
        );
    }

    /**
     * Crée un ticket en attente de validation.
     *
     * @param ticketId identifiant du ticket
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @param amount montant du ticket
     * @return ticket de test
     */
    private Ticket createPendingTicket(
            UUID ticketId,
            UUID merchantId,
            UUID customerId,
            BigDecimal amount
    ) {
        return new Ticket(
                ticketId,
                merchantId,
                customerId,
                "TICKET-" + ticketId,
                "fingerprint-" + ticketId,
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                amount,
                "OCR TEXT",
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }
}