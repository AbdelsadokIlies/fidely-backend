package com.fidely.backend.application.Services;

import com.fidely.backend.application.LoyaltyService;
import com.fidely.backend.application.LoyaltyTransactionService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
}