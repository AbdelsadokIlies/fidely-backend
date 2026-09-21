package com.fidely.backend.application.Services;

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

@ExtendWith(MockitoExtension.class)
class LoyaltyTransactionServiceTest {

    @Mock
    private ILoyaltyRepository loyaltyRepository;

    @Mock
    private ITicketService ticketService;

    @Mock
    private IPointRuleService pointRuleService;

    private LoyaltyTransactionService loyaltyTransactionService;

    @BeforeEach
    void setUp() {
        loyaltyTransactionService =
                new LoyaltyTransactionService(
                        loyaltyRepository,
                        ticketService,
                        pointRuleService
                );
    }

    @Test
    void shouldAddPointsAndValidateTicket() {
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                amount
        );

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        PointRule pointRule = createPointRule();

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(loyaltyRepository.findTransactionsByTicketId(ticketId))
                .thenReturn(List.of());

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(pointRule));

        when(loyaltyRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        )).thenReturn(Optional.of(loyalty));

        loyaltyTransactionService.addPointsFromTicket(ticketId);

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        verify(loyaltyRepository)
                .save(loyalty);

        verify(loyaltyRepository)
                .saveTransaction(
                        any(LoyaltyTransaction.class)
                );

        verify(ticketService)
                .validateTicket(ticketId);
    }

    @Test
    void shouldRejectWhenTicketDoesNotExist() {
        UUID ticketId = UUID.randomUUID();

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyTransactionService
                        .addPointsFromTicket(ticketId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Ticket introuvable : " + ticketId
                );

        verifyNoInteractions(pointRuleService);

        verify(loyaltyRepository, never())
                .save(any());

        verify(loyaltyRepository, never())
                .saveTransaction(any());

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

    @Test
    void shouldRejectWhenPointsWereAlreadyAwarded() {
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("50.00")
        );

        LoyaltyTransaction existingTransaction =
                createTransaction(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ticketId,
                        50
                );

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(loyaltyRepository.findTransactionsByTicketId(ticketId))
                .thenReturn(List.of(existingTransaction));

        assertThatThrownBy(() ->
                loyaltyTransactionService
                        .addPointsFromTicket(ticketId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Points already awarded for ticket : "
                                + ticketId
                );

        verifyNoInteractions(pointRuleService);

        verify(loyaltyRepository, never())
                .save(any());

        verify(loyaltyRepository, never())
                .saveTransaction(any());

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

    @Test
    void shouldRejectWhenNoValidPointRuleExists() {
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("50.00")
        );

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(loyaltyRepository.findTransactionsByTicketId(ticketId))
                .thenReturn(List.of());

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyTransactionService
                        .addPointsFromTicket(ticketId)
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

    @Test
    void shouldRejectWhenLoyaltyDoesNotExist() {
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("50.00")
        );

        PointRule pointRule = createPointRule();

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(loyaltyRepository.findTransactionsByTicketId(ticketId))
                .thenReturn(List.of());

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(pointRule));

        when(loyaltyRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyTransactionService
                        .addPointsFromTicket(ticketId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Loyalty introuvable pour le client : "
                                + customerId
                );

        verify(loyaltyRepository, never())
                .save(any());

        verify(loyaltyRepository, never())
                .saveTransaction(any());

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

    @Test
    void shouldRejectWhenCalculatedPointsAreZero() {
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

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
                LocalDateTime.of(
                        2026,
                        1,
                        1,
                        0,
                        0
                ),
                LocalDateTime.of(
                        2026,
                        12,
                        31,
                        23,
                        59
                ),
                LocalDateTime.of(
                        2026,
                        1,
                        1,
                        0,
                        0
                )
        );

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(loyaltyRepository.findTransactionsByTicketId(ticketId))
                .thenReturn(List.of());

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(pointRule));

        assertThatThrownBy(() ->
                loyaltyTransactionService
                        .addPointsFromTicket(ticketId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Le ticket ne génère aucun point : "
                                + ticketId
                );

        verify(loyaltyRepository, never())
                .save(any());

        verify(loyaltyRepository, never())
                .saveTransaction(any());

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

    @Test
    void shouldNotValidateTicketWhenTransactionCannotBeSaved() {
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();

        Ticket ticket = createPendingTicket(
                ticketId,
                merchantId,
                customerId,
                new BigDecimal("112.00")
        );

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        PointRule pointRule = createPointRule();

        when(ticketService.getTicketById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(loyaltyRepository.findTransactionsByTicketId(ticketId))
                .thenReturn(List.of());

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(pointRule));

        when(loyaltyRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        )).thenReturn(Optional.of(loyalty));

        doThrow(new IllegalStateException(
                "Erreur lors de la sauvegarde"
        ))
                .when(loyaltyRepository)
                .saveTransaction(any());

        assertThatThrownBy(() ->
                loyaltyTransactionService
                        .addPointsFromTicket(ticketId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Erreur lors de la sauvegarde"
                );

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        verify(loyaltyRepository)
                .save(loyalty);

        verify(loyaltyRepository)
                .saveTransaction(any());

        verify(ticketService, never())
                .validateTicket(ticketId);
    }

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
                LocalDate.of(
                        2026,
                        6,
                        15
                ),
                LocalTime.of(
                        12,
                        30
                ),
                amount,
                "OCR TEXT",
                "storage/" + ticketId,
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }
}