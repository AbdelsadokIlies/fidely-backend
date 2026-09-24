package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**

 * Tests unitaires du service responsable des transactions
 * de fidélité.
 *
 * <p>Ces tests vérifient notamment l'attribution manuelle de points
 * à partir d'une transaction enregistrée directement par un marchand.</p>
 */
@ExtendWith(MockitoExtension.class)
class LoyaltyTransactionServiceTest {

    @Mock
    private ILoyaltyRepository loyaltyRepository;

    @Mock
    private ITicketService ticketService;

    @Mock
    private IPointRuleService pointRuleService;

    @Mock
    private PointRule pointRule;

    private LoyaltyTransactionService loyaltyTransactionService;

    @BeforeEach
    void setUp() {
        loyaltyTransactionService = new LoyaltyTransactionService(
                loyaltyRepository,
                ticketService,
                pointRuleService
        );
    }

    /**

     * Vérifie qu'une transaction manuelle ajoute les points
     * correspondants à la fidélité et crée une transaction
     * sans référence à un ticket.
     */
    @Test
    void shouldAddPointsManually() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        LocalDateTime createdAt = LocalDateTime.of(
                2026,
                6,
                15,
                12,
                0
        );

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                0,
                createdAt,
                createdAt
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        ))
                .thenReturn(Optional.of(pointRule));

        when(pointRule.calculatePoints(amount))
                .thenReturn(112);

        Loyalty result = loyaltyTransactionService.addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                amount
        );

        assertThat(result)
                .isSameAs(loyalty);

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        verify(loyaltyRepository)
                .save(loyalty);

        ArgumentCaptor<LoyaltyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(LoyaltyTransaction.class);

        verify(loyaltyRepository)
                .saveTransaction(transactionCaptor.capture());

        LoyaltyTransaction transaction =
                transactionCaptor.getValue();

        assertThat(transaction.getId())
                .isNotNull();

        assertThat(transaction.getLoyaltyId())
                .isEqualTo(loyaltyId);

        assertThat(transaction.getTicketId())
                .isNull();

        assertThat(transaction.getPoints())
                .isEqualTo(112);

        assertThat(transaction.getDescription())
                .isEqualTo(
                        "Points gagnés sur une transaction manuelle"
                );

        assertThat(transaction.getCreatedAt())
                .isNotNull();

        verify(pointRule)
                .calculatePoints(amount);

        verify(pointRuleService)
                .getValidPointRule(
                        eq(merchantId),
                        any(LocalDateTime.class)
                );

        verifyNoInteractions(ticketService);
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée
     * lorsqu'aucune fidélité correspondante n'existe.
     */
    @Test
    void shouldRejectManualTransactionWhenLoyaltyDoesNotExist() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        amount
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Loyalty introuvable : " + loyaltyId
                );
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée lorsque
     * la fidélité appartient à un autre marchand.
     */
    @Test
    void shouldRejectManualTransactionWhenMerchantDoesNotMatch() {
        UUID loyaltyId = UUID.randomUUID();
        UUID loyaltyMerchantId = UUID.randomUUID();
        UUID providedMerchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                loyaltyMerchantId
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        providedMerchantId,
                        customerId,
                        amount
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "La fidélité n'appartient pas au marchand fourni."
                );
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée lorsque
     * la fidélité appartient à un autre client.
     */
    @Test
    void shouldRejectManualTransactionWhenCustomerDoesNotMatch() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID loyaltyCustomerId = UUID.randomUUID();
        UUID providedCustomerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                loyaltyCustomerId,
                merchantId
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        providedCustomerId,
                        amount
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "La fidélité n'appartient pas au client fourni."
                );
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée
     * lorsque le marchand est absent.
     */
    @Test
    void shouldRejectManualTransactionWhenMerchantIsNull() {
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        null,
                        customerId,
                        amount
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Merchant cannot be null");
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée
     * lorsque le client est absent.
     */
    @Test
    void shouldRejectManualTransactionWhenCustomerIsNull() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        null,
                        amount
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer cannot be null");
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée
     * lorsque le montant est absent.
     */
    @Test
    void shouldRejectManualTransactionWhenAmountIsNull() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount cannot be null");
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée
     * lorsque le montant est nul.
     */
    @Test
    void shouldRejectManualTransactionWhenAmountIsZero() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        BigDecimal.ZERO
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée
     * lorsque le montant est négatif.
     */
    @Test
    void shouldRejectManualTransactionWhenAmountIsNegative() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        new BigDecimal("-10.00")
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");
    }

    /**
     * Crée une fidélité de test.
     *
     * @param loyaltyId identifiant de la fidélité
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     * @return fidélité créée
     */
    private Loyalty createLoyalty(
            UUID loyaltyId,
            UUID customerId,
            UUID merchantId
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                0,
                now,
                now
        );
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée lorsqu'aucune
     * règle de points valide n'existe pour le marchand.
     */
    @Test
    void shouldRejectManualTransactionWhenNoValidPointRuleExists() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        amount
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Aucune règle de points valide pour le marchand : "
                                + merchantId
                );

        verify(pointRuleService).getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        );

        verifyNoMoreInteractions(pointRuleService);
    }

    /**
     * Vérifie qu'une transaction manuelle est refusée lorsque
     * la règle de points ne génère aucun point.
     */
    @Test
    void shouldRejectManualTransactionWhenNoPointIsGenerated() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("0.10");

        Loyalty loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.of(loyalty));

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        ))
                .thenReturn(Optional.of(pointRule));

        when(pointRule.calculatePoints(amount))
                .thenReturn(0);

        assertThatThrownBy(() ->
                loyaltyTransactionService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        amount
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "La transaction ne génère aucun point."
                );

        assertThat(loyalty.getPointsBalance())
                .isZero();

        verify(pointRule).calculatePoints(amount);

        verify(loyaltyRepository, never())
                .save(any(Loyalty.class));

        verify(loyaltyRepository, never())
                .saveTransaction(any(LoyaltyTransaction.class));
    }

}
