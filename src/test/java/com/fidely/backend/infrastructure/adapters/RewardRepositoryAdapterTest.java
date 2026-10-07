package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import com.fidely.backend.infrastructure.entities.models.loyalties.Rewards.RewardEntity;
import com.fidely.backend.infrastructure.mappers.loyalties.Rewards.RewardMapper;
import com.fidely.backend.infrastructure.repositories.loyalties.Rewards.SpringDataRewardRepository;
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
 * Tests unitaires de l'adaptateur repository des récompenses.
 */
@ExtendWith(MockitoExtension.class)
class RewardRepositoryAdapterTest {

    @Mock
    private SpringDataRewardRepository repository;

    @Mock
    private RewardMapper mapper;

    private RewardRepositoryAdapter adapter;

    private UUID rewardId;
    private UUID merchantId;

    @BeforeEach
    void setUp() {
        adapter = new RewardRepositoryAdapter(
                repository,
                mapper
        );

        rewardId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
    }

    @Test
    void shouldFindRewardById() {
        RewardEntity entity = createRewardEntity();
        Reward reward = createReward();

        when(repository.findById(rewardId))
                .thenReturn(Optional.of(entity));

        when(mapper.toDomain(entity))
                .thenReturn(reward);

        Optional<Reward> result =
                adapter.findById(rewardId);

        assertThat(result)
                .isPresent()
                .contains(reward);

        verify(repository)
                .findById(rewardId);

        verify(mapper)
                .toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenRewardDoesNotExist() {
        when(repository.findById(rewardId))
                .thenReturn(Optional.empty());

        Optional<Reward> result =
                adapter.findById(rewardId);

        assertThat(result)
                .isEmpty();

        verify(repository)
                .findById(rewardId);
    }

    @Test
    void shouldRejectNullRewardIdWhenFindingById() {
        assertThatThrownBy(() ->
                adapter.findById(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward id cannot be null");

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void shouldFindRewardsByMerchantId() {
        RewardEntity firstEntity = createRewardEntity();
        RewardEntity secondEntity = createRewardEntity();

        Reward firstReward = createReward();
        Reward secondReward = createReward();

        when(repository.findByMerchantId(merchantId))
                .thenReturn(List.of(
                        firstEntity,
                        secondEntity
                ));

        when(mapper.toDomain(firstEntity))
                .thenReturn(firstReward);

        when(mapper.toDomain(secondEntity))
                .thenReturn(secondReward);

        List<Reward> result =
                adapter.findByMerchantId(merchantId);

        assertThat(result)
                .hasSize(2)
                .containsExactly(
                        firstReward,
                        secondReward
                );

        verify(repository)
                .findByMerchantId(merchantId);

        verify(mapper)
                .toDomain(firstEntity);

        verify(mapper)
                .toDomain(secondEntity);
    }

    @Test
    void shouldReturnEmptyListWhenMerchantHasNoRewards() {
        when(repository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        List<Reward> result =
                adapter.findByMerchantId(merchantId);

        assertThat(result)
                .isEmpty();

        verify(repository)
                .findByMerchantId(merchantId);
    }

    @Test
    void shouldRejectNullMerchantIdWhenFindingRewards() {
        assertThatThrownBy(() ->
                adapter.findByMerchantId(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Merchant id cannot be null");

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void shouldSaveReward() {
        Reward reward = createReward();
        RewardEntity entity = createRewardEntity();
        RewardEntity savedEntity = createRewardEntity();
        Reward savedReward = createReward();

        when(mapper.toEntity(reward))
                .thenReturn(entity);

        when(repository.save(entity))
                .thenReturn(savedEntity);

        when(mapper.toDomain(savedEntity))
                .thenReturn(savedReward);

        Reward result =
                adapter.save(reward);

        assertThat(result)
                .isEqualTo(savedReward);

        verify(mapper)
                .toEntity(reward);

        verify(repository)
                .save(entity);

        verify(mapper)
                .toDomain(savedEntity);
    }

    @Test
    void shouldRejectNullRewardWhenSaving() {
        assertThatThrownBy(() ->
                adapter.save(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward cannot be null");

        verifyNoInteractions(repository, mapper);
    }

    @Test
    void shouldDeleteRewardById() {
        adapter.deleteById(rewardId);

        verify(repository)
                .deleteById(rewardId);
    }

    @Test
    void shouldRejectNullRewardIdWhenDeleting() {
        assertThatThrownBy(() ->
                adapter.deleteById(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward id cannot be null");

        verifyNoInteractions(repository);
    }

    /**
     * Crée une récompense utilisée dans les tests.
     *
     * @return récompense de test
     */
    private Reward createReward() {
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 6, 1, 0, 0);

        return new Reward(
                rewardId,
                merchantId,
                "Café offert",
                "Un café offert",
                100,
                10,
                createdAt,
                null,
                true,
                createdAt
        );
    }

    /**
     * Crée une entité récompense utilisée dans les tests.
     *
     * @return entité récompense de test
     */
    private RewardEntity createRewardEntity() {
        return new RewardEntity(
                rewardId,
                merchantId,
                "Café offert",
                "Un café offert",
                100,
                10,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                null,
                true,
                LocalDateTime.of(2026, 6, 1, 0, 0)
        );
    }
}