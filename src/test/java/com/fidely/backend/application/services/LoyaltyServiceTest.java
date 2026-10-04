package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private ILoyaltyRepository loyaltyRepository;

    @Mock
    private LoyaltyTransactionService loyaltyTransactionService;

    private LoyaltyService loyaltyService;

    @BeforeEach
    void setUp() {
        loyaltyService = new LoyaltyService(
                loyaltyRepository,
                loyaltyTransactionService
        );
    }

    @Test
    void shouldCreateLoyalty() {
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Loyalty savedLoyalty = new Loyalty(
                UUID.randomUUID(),
                customerId,
                merchantId,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loyaltyRepository.save(any(Loyalty.class)))
                .thenReturn(savedLoyalty);

        Loyalty result = loyaltyService.createLoyalty(
                customerId,
                merchantId
        );

        assertThat(result).isEqualTo(savedLoyalty);

        verify(loyaltyRepository).save(
                org.mockito.ArgumentMatchers.argThat(
                        loyalty ->
                                loyalty.getCustomerId().equals(customerId)
                                        && loyalty.getMerchantId().equals(merchantId)
                                        && loyalty.getPointsBalance() == 0
                )
        );
    }

    @Test
    void shouldAddPointsManually() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                112,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loyaltyTransactionService.addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                amount
        )).thenReturn(loyalty);

        Loyalty result = loyaltyService.addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                amount
        );

        assertThat(result).isEqualTo(loyalty);

        verify(loyaltyTransactionService).addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                amount
        );

        verifyNoMoreInteractions(loyaltyTransactionService);
    }

    @Test
    void shouldRetryManualTransactionAfterOptimisticLockingFailure() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        Loyalty loyalty = new Loyalty(
                loyaltyId,
                customerId,
                merchantId,
                112,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loyaltyTransactionService.addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                amount
        ))
                .thenThrow(new OptimisticLockingFailureException(
                        "Optimistic locking conflict"
                ))
                .thenReturn(loyalty);

        Loyalty result = loyaltyService.addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                amount
        );

        assertThat(result).isEqualTo(loyalty);

        verify(loyaltyTransactionService, times(2))
                .addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        amount
                );
    }

    @Test
    void shouldPropagateOptimisticLockingFailureAfterMaxRetries() {
        UUID loyaltyId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("112.00");

        when(loyaltyTransactionService.addPointsManually(
                loyaltyId,
                merchantId,
                customerId,
                amount
        ))
                .thenThrow(new OptimisticLockingFailureException(
                        "Optimistic locking conflict"
                ));

        assertThatThrownBy(() ->
                loyaltyService.addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        amount
                )
        )
                .isInstanceOf(OptimisticLockingFailureException.class);

        verify(loyaltyTransactionService, times(3))
                .addPointsManually(
                        loyaltyId,
                        merchantId,
                        customerId,
                        amount
                );
    }

    @Test
    void shouldGetTransactionsByCustomer() {
        UUID customerId = UUID.randomUUID();

        UUID loyaltyId1 = UUID.randomUUID();
        UUID loyaltyId2 = UUID.randomUUID();

        UUID merchantId1 = UUID.randomUUID();
        UUID merchantId2 = UUID.randomUUID();

        Loyalty loyalty1 = new Loyalty(
                loyaltyId1,
                customerId,
                merchantId1,
                100,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Loyalty loyalty2 = new Loyalty(
                loyaltyId2,
                customerId,
                merchantId2,
                200,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        LoyaltyTransaction transaction1 = new LoyaltyTransaction(
                UUID.randomUUID(),
                loyaltyId1,
                UUID.randomUUID(),
                10,
                "Achat",
                LocalDateTime.now()
        );

        LoyaltyTransaction transaction2 = new LoyaltyTransaction(
                UUID.randomUUID(),
                loyaltyId2,
                UUID.randomUUID(),
                20,
                "Achat",
                LocalDateTime.now()
        );

        when(loyaltyRepository.findByCustomerId(customerId))
                .thenReturn(List.of(loyalty1, loyalty2));

        when(loyaltyRepository.findTransactionsByLoyaltyId(loyaltyId1))
                .thenReturn(List.of(transaction1));

        when(loyaltyRepository.findTransactionsByLoyaltyId(loyaltyId2))
                .thenReturn(List.of(transaction2));

        List<LoyaltyTransaction> result =
                loyaltyService.getTransactionsByCustomer(customerId);

        assertThat(result)
                .containsExactly(transaction1, transaction2);

        verify(loyaltyRepository).findByCustomerId(customerId);

        verify(loyaltyRepository)
                .findTransactionsByLoyaltyId(loyaltyId1);

        verify(loyaltyRepository)
                .findTransactionsByLoyaltyId(loyaltyId2);
    }

    @Test
    void shouldReturnEmptyTransactionsWhenCustomerHasNoLoyalty() {
        UUID customerId = UUID.randomUUID();

        when(loyaltyRepository.findByCustomerId(customerId))
                .thenReturn(List.of());

        List<LoyaltyTransaction> result =
                loyaltyService.getTransactionsByCustomer(customerId);

        assertThat(result).isEmpty();

        verify(loyaltyRepository).findByCustomerId(customerId);

        verifyNoMoreInteractions(loyaltyRepository);
    }

    @Test
    void shouldGetTransactionsByMerchant() {
        UUID merchantId = UUID.randomUUID();

        UUID loyaltyId1 = UUID.randomUUID();
        UUID loyaltyId2 = UUID.randomUUID();

        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();

        Loyalty loyalty1 = new Loyalty(
                loyaltyId1,
                customerId1,
                merchantId,
                100,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Loyalty loyalty2 = new Loyalty(
                loyaltyId2,
                customerId2,
                merchantId,
                200,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        LoyaltyTransaction transaction1 = new LoyaltyTransaction(
                UUID.randomUUID(),
                loyaltyId1,
                UUID.randomUUID(),
                10,
                "Achat",
                LocalDateTime.now()
        );

        LoyaltyTransaction transaction2 = new LoyaltyTransaction(
                UUID.randomUUID(),
                loyaltyId2,
                UUID.randomUUID(),
                20,
                "Achat",
                LocalDateTime.now()
        );

        when(loyaltyRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(loyalty1, loyalty2));

        when(loyaltyRepository.findTransactionsByLoyaltyId(loyaltyId1))
                .thenReturn(List.of(transaction1));

        when(loyaltyRepository.findTransactionsByLoyaltyId(loyaltyId2))
                .thenReturn(List.of(transaction2));

        List<LoyaltyTransaction> result =
                loyaltyService.getTransactionsByMerchant(merchantId);

        assertThat(result)
                .containsExactly(transaction1, transaction2);

        verify(loyaltyRepository).findByMerchantId(merchantId);

        verify(loyaltyRepository)
                .findTransactionsByLoyaltyId(loyaltyId1);

        verify(loyaltyRepository)
                .findTransactionsByLoyaltyId(loyaltyId2);
    }

    @Test
    void shouldReturnEmptyTransactionsWhenMerchantHasNoLoyalty() {
        UUID merchantId = UUID.randomUUID();

        when(loyaltyRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        List<LoyaltyTransaction> result =
                loyaltyService.getTransactionsByMerchant(merchantId);

        assertThat(result).isEmpty();

        verify(loyaltyRepository).findByMerchantId(merchantId);

        verifyNoMoreInteractions(loyaltyRepository);
    }
}