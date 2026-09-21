package com.fidely.backend.application.Services;

import com.fidely.backend.application.LoyaltyService;
import com.fidely.backend.application.LoyaltyTransactionService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    void shouldRejectAddPointsWhenLoyaltyDoesNotExist() {
        UUID loyaltyId = UUID.randomUUID();

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyService.addPoints(loyaltyId, 50)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Loyalty introuvable : " + loyaltyId
                );

        verify(loyaltyRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectRemovePointsWhenLoyaltyDoesNotExist() {
        UUID loyaltyId = UUID.randomUUID();

        when(loyaltyRepository.findById(loyaltyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                loyaltyService.removePoints(loyaltyId, 50)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Loyalty introuvable : " + loyaltyId
                );

        verify(loyaltyRepository, never())
                .save(any());
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
                argThat(loyalty ->
                        loyalty.getCustomerId().equals(customerId)
                                && loyalty.getMerchantId().equals(merchantId)
                                && loyalty.getPointsBalance() == 0
                )
        );
    }
}