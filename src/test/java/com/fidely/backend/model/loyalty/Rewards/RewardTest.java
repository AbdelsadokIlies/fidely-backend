package com.fidely.backend.model.loyalty.Rewards;

import com.fidely.backend.domain.model.Merchants.Merchant;
import com.fidely.backend.domain.model.loyalty.Rewards.Reward;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RewardTest {

    private final Merchant merchant = mock(Merchant.class);

    private final UUID id = UUID.randomUUID();
    private final String name = "10€ de réduction";
    private final String description = "Réduction de 10€";
    private final int costPoints = 100;
    private final LocalDateTime validFrom = LocalDateTime.of(2026, 9, 1, 0, 0);
    private final LocalDateTime validTo = LocalDateTime.of(2026, 9, 30, 23, 59);
    private final LocalDateTime createdAt = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test
    void shouldBeAvailableWhenAllConditionsAreMet() {
        Reward reward = createReward(
                true,
                10
        );

        LocalDateTime date = LocalDateTime.of(2026, 9, 20, 15, 0);

        assertTrue(reward.isAvailableAt(date));
    }

    @Test
    void shouldNotBeAvailableWhenInactive() {
        Reward reward = createReward(
                false,
                10
        );

        LocalDateTime date = LocalDateTime.of(2026, 9, 20, 15, 0);

        assertFalse(reward.isAvailableAt(date));
    }

    @Test
    void shouldNotBeAvailableBeforeValidFrom() {
        Reward reward = createReward(
                true,
                10
        );

        LocalDateTime date = LocalDateTime.of(2026, 8, 31, 23, 59);

        assertFalse(reward.isAvailableAt(date));
    }

    @Test
    void shouldNotBeAvailableAfterValidTo() {
        Reward reward = createReward(
                true,
                10
        );

        LocalDateTime date = LocalDateTime.of(2026, 10, 1, 0, 0);

        assertFalse(reward.isAvailableAt(date));
    }

    @Test
    void shouldNotBeAvailableWhenQuantityIsZero() {
        Reward reward = createReward(
                true,
                0
        );

        LocalDateTime date = LocalDateTime.of(2026, 9, 20, 15, 0);

        assertFalse(reward.isAvailableAt(date));
    }

    @Test
    void shouldBeAvailableWhenQuantityIsUnlimited() {
        Reward reward = createReward(
                true,
                null
        );

        LocalDateTime date = LocalDateTime.of(2026, 9, 20, 15, 0);

        assertTrue(reward.isAvailableAt(date));
    }

    @Test
    void shouldBeAvailableAtValidFrom() {
        Reward reward = createReward(
                true,
                10
        );

        assertTrue(reward.isAvailableAt(validFrom));
    }

    @Test
    void shouldBeAvailableAtValidTo() {
        Reward reward = createReward(
                true,
                10
        );

        assertTrue(reward.isAvailableAt(validTo));
    }

    private Reward createReward(
            boolean active,
            Integer quantityAvailable
    ) {
        return new Reward(
                id,
                merchant,
                name,
                description,
                costPoints,
                quantityAvailable,
                validFrom,
                validTo,
                active,
                createdAt
        );
    }

}
