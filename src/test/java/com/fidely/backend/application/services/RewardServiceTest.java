package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.IRewardRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service applicatif de gestion des récompenses.
 */
@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @Mock
    private IRewardRepository rewardRepository;

    @Mock
    private IUserRepository userRepository;

    private RewardService rewardService;

    private UUID rewardId;
    private UUID merchantId;
    private UUID userId;
    private Reward reward;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(
                rewardRepository,
                userRepository
        );

        rewardId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
        userId = UUID.randomUUID();

        reward = new Reward(
                rewardId,
                merchantId,
                "10% de réduction",
                "Une réduction de 10% sur votre prochain achat",
                100,
                10,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(1),
                true,
                LocalDateTime.now()
        );
    }

    @Test
    void shouldCreateReward() {
        when(userRepository.getMerchantId(userId))
                .thenReturn(merchantId);

        when(rewardRepository.save(reward))
                .thenReturn(reward);

        Reward result =
                rewardService.createReward(userId, reward);

        assertThat(result)
                .isEqualTo(reward);

        verify(userRepository)
                .getMerchantId(userId);

        verify(rewardRepository)
                .save(reward);
    }

    @Test
    void shouldRejectNullRewardWhenCreating() {
        assertThatThrownBy(() ->
                rewardService.createReward(userId, null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward cannot be null");

        verifyNoInteractions(
                rewardRepository,
                userRepository
        );
    }

    @Test
    void shouldGetRewardById() {
        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.of(reward));

        Optional<Reward> result =
                rewardService.getRewardById(rewardId);

        assertThat(result)
                .isPresent()
                .contains(reward);

        verify(rewardRepository)
                .findById(rewardId);
    }

    @Test
    void shouldReturnEmptyWhenRewardDoesNotExist() {
        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.empty());

        Optional<Reward> result =
                rewardService.getRewardById(rewardId);

        assertThat(result)
                .isEmpty();

        verify(rewardRepository)
                .findById(rewardId);
    }

    @Test
    void shouldRejectNullRewardIdWhenGettingReward() {
        assertThatThrownBy(() ->
                rewardService.getRewardById(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward id cannot be null");

        verifyNoInteractions(
                rewardRepository,
                userRepository
        );
    }

    @Test
    void shouldGetRewardsByMerchant() {
        when(rewardRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(reward));

        List<Reward> result =
                rewardService.getRewardsByMerchant(merchantId);

        assertThat(result)
                .hasSize(1)
                .containsExactly(reward);

        verify(rewardRepository)
                .findByMerchantId(merchantId);
    }

    @Test
    void shouldReturnEmptyListWhenMerchantHasNoRewards() {
        when(rewardRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        List<Reward> result =
                rewardService.getRewardsByMerchant(merchantId);

        assertThat(result)
                .isEmpty();

        verify(rewardRepository)
                .findByMerchantId(merchantId);
    }

    @Test
    void shouldRejectNullMerchantId() {
        assertThatThrownBy(() ->
                rewardService.getRewardsByMerchant(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Merchant id cannot be null");

        verifyNoInteractions(
                rewardRepository,
                userRepository
        );
    }

    @Test
    void shouldUpdateReward() {
        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.of(reward));

        when(rewardRepository.save(reward))
                .thenReturn(reward);

        Reward result =
                rewardService.updateReward(reward);

        assertThat(result)
                .isEqualTo(reward);

        verify(rewardRepository)
                .findById(rewardId);

        verify(rewardRepository)
                .save(reward);
    }

    @Test
    void shouldRejectNullRewardWhenUpdating() {
        assertThatThrownBy(() ->
                rewardService.updateReward(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward cannot be null");

        verifyNoInteractions(
                rewardRepository,
                userRepository
        );
    }

    @Test
    void shouldRejectRewardWithoutIdWhenUpdating() {
        Reward rewardWithoutId = new Reward(
                null,
                merchantId,
                "10% de réduction",
                "Une réduction de 10% sur votre prochain achat",
                100,
                10,
                LocalDateTime.now(),
                LocalDateTime.now().plusMonths(1),
                true,
                LocalDateTime.now()
        );

        assertThatThrownBy(() ->
                rewardService.updateReward(rewardWithoutId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward id cannot be null");

        verifyNoInteractions(
                rewardRepository,
                userRepository
        );
    }

    @Test
    void shouldRejectUpdateWhenRewardDoesNotExist() {
        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                rewardService.updateReward(reward)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward not found");

        verify(rewardRepository)
                .findById(rewardId);
    }

    @Test
    void shouldDeleteReward() {
        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.of(reward));

        rewardService.deleteReward(rewardId);

        verify(rewardRepository)
                .findById(rewardId);

        verify(rewardRepository)
                .deleteById(rewardId);
    }

    @Test
    void shouldRejectNullRewardIdWhenDeleting() {
        assertThatThrownBy(() ->
                rewardService.deleteReward(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward id cannot be null");

        verifyNoInteractions(
                rewardRepository,
                userRepository
        );
    }

    @Test
    void shouldRejectDeleteWhenRewardDoesNotExist() {
        when(rewardRepository.findById(rewardId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                rewardService.deleteReward(rewardId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward not found");

        verify(rewardRepository)
                .findById(rewardId);
    }
}