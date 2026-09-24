package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

}