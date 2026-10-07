package com.fidely.backend.infrastructure.SpringSataRepositories;

import com.fidely.backend.IntegrationTest;
import com.fidely.backend.infrastructure.entities.models.loyalties.Rewards.RewardEntity;
import com.fidely.backend.infrastructure.repositories.loyalties.Rewards.SpringDataRewardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration du repository Spring Data des récompenses.
 */
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class SpringDataRewardRepositoryTest extends IntegrationTest {

    @Autowired
    private SpringDataRewardRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndFindRewardById() {
        UUID rewardId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);

        RewardEntity reward = createReward(
                rewardId,
                merchantId,
                "Café offert"
        );

        repository.saveAndFlush(reward);

        Optional<RewardEntity> result =
                repository.findById(rewardId);

        assertThat(result)
                .isPresent()
                .get()
                .extracting(RewardEntity::getId)
                .isEqualTo(rewardId);
    }

    @Test
    void shouldFindRewardsByMerchantId() {
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);

        repository.saveAndFlush(
                createReward(
                        UUID.randomUUID(),
                        merchantId,
                        "Café offert"
                )
        );

        repository.saveAndFlush(
                createReward(
                        UUID.randomUUID(),
                        merchantId,
                        "Dessert offert"
                )
        );

        List<RewardEntity> result =
                repository.findByMerchantId(merchantId);

        assertThat(result)
                .hasSize(2)
                .extracting(RewardEntity::getMerchantId)
                .containsOnly(merchantId);
    }

    @Test
    void shouldReturnEmptyWhenMerchantHasNoRewards() {
        List<RewardEntity> result =
                repository.findByMerchantId(UUID.randomUUID());

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldNotReturnRewardsFromAnotherMerchant() {
        UUID merchantId = UUID.randomUUID();
        UUID otherMerchantId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertMerchant(otherMerchantId);

        repository.saveAndFlush(
                createReward(
                        UUID.randomUUID(),
                        merchantId,
                        "Café offert"
                )
        );

        repository.saveAndFlush(
                createReward(
                        UUID.randomUUID(),
                        otherMerchantId,
                        "Dessert offert"
                )
        );

        List<RewardEntity> result =
                repository.findByMerchantId(merchantId);

        assertThat(result)
                .hasSize(1)
                .extracting(RewardEntity::getMerchantId)
                .containsOnly(merchantId);

        assertThat(result)
                .extracting(RewardEntity::getName)
                .containsExactly("Café offert");
    }

    @Test
    void shouldReturnEmptyWhenRewardDoesNotExist() {
        Optional<RewardEntity> result =
                repository.findById(UUID.randomUUID());

        assertThat(result)
                .isEmpty();
    }

    /**
     * Crée une entité récompense utilisée dans les tests.
     *
     * @param id identifiant de la récompense
     * @param merchantId identifiant du marchand
     * @param name nom de la récompense
     * @return récompense de test
     */
    private RewardEntity createReward(
            UUID id,
            UUID merchantId,
            String name
    ) {
        LocalDateTime now =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        return new RewardEntity(
                id,
                merchantId,
                name,
                "Récompense de test",
                100,
                10,
                now,
                null,
                true,
                now
        );
    }

    /**
     * Insère un marchand nécessaire aux contraintes de clé étrangère.
     *
     * @param id identifiant du marchand
     */
    private void insertMerchant(UUID id) {
        jdbcTemplate.update(
                "INSERT INTO merchants (id, name, slug) " +
                        "VALUES (?, ?, ?) " +
                        "ON CONFLICT (id) DO NOTHING",
                id,
                "Merchant " + id,
                "merchant-" + id
        );
    }
}