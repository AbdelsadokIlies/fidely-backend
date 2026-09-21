package com.fidely.backend.domain.models.loyalties.Rewards;

import com.fidely.backend.domain.models.loyalties.Rewards.RedemptionStatus;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import com.fidely.backend.domain.models.loyalties.Rewards.RewardRedemption;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RewardRedemptionTest {

    private final UUID id = UUID.randomUUID();
    private final UUID rewardId = UUID.randomUUID();
    private final UUID loyaltyId = UUID.randomUUID();
    private final String qrToken = "QR-TOKEN-123";
    private final LocalDateTime createdAt = LocalDateTime.of(2026, 9, 20, 10, 0);
    private final LocalDateTime expiresAt = LocalDateTime.of(2026, 9, 20, 18, 0);

    @Test
    void shouldCreatePendingRedemption() {
        RewardRedemption redemption = createRedemption();

        assertNotNull(redemption);
        assertEquals(id, redemption.getId());
        assertEquals(rewardId, redemption.getRewardId());
        assertEquals(loyaltyId, redemption.getLoyaltyId());
        assertEquals(qrToken, redemption.getQrToken());
        assertEquals(RedemptionStatus.PENDING, redemption.getStatus());
        assertNull(redemption.getRedeemedAt());
        assertEquals(expiresAt, redemption.getExpiresAt());
        assertEquals(createdAt, redemption.getCreatedAt());
    }

    @Test
    void shouldConsumePendingRedemption() {
        RewardRedemption redemption = createRedemption();
        LocalDateTime consumedAt = LocalDateTime.of(2026, 9, 20, 15, 0);

        redemption.consume(consumedAt);

        assertEquals(RedemptionStatus.CONSUMED, redemption.getStatus());
        assertEquals(consumedAt, redemption.getRedeemedAt());
    }

    @Test
    void shouldExpireRedemptionWhenConsumedAfterExpiration() {
        RewardRedemption redemption = createRedemption();
        LocalDateTime consumedAt = LocalDateTime.of(2026, 9, 20, 19, 0);

        assertThrows(
                IllegalStateException.class,
                () -> redemption.consume(consumedAt)
        );

        assertEquals(RedemptionStatus.EXPIRED, redemption.getStatus());
        assertNull(redemption.getRedeemedAt());
    }

    @Test
    void shouldRejectConsumptionWhenRedemptionIsNotPending() {
        RewardRedemption redemption = createRedemptionWithStatus(
                RedemptionStatus.CONSUMED
        );

        assertThrows(
                IllegalStateException.class,
                () -> redemption.consume(
                        LocalDateTime.of(2026, 9, 20, 15, 0)
                )
        );

        assertEquals(RedemptionStatus.CONSUMED, redemption.getStatus());
    }

    private RewardRedemption createRedemption() {
        return createRedemptionWithStatus(RedemptionStatus.PENDING);
    }

    private RewardRedemption createRedemptionWithStatus(
            RedemptionStatus status
    ) {
        return new RewardRedemption(
                id,
                rewardId,
                loyaltyId,
                qrToken,
                status,
                null,
                expiresAt,
                createdAt
        );
    }

}
