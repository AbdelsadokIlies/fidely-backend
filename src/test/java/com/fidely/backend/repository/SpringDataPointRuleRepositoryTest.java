package com.fidely.backend.repository;

import com.fidely.backend.repository.entities.loyalties.Rewards.PointRuleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class SpringDataPointRuleRepositoryTest {

    @Autowired
    private SpringDataPointRuleRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndFindPointRuleById() {
        UUID merchantId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        insertMerchant(merchantId);

        PointRuleEntity pointRule =
                createPointRule(
                        pointRuleId,
                        merchantId,
                        new BigDecimal("1.5000"),
                        (short) 1
                );

        repository.save(pointRule);

        Optional<PointRuleEntity> result =
                repository.findById(pointRuleId);

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(entity -> {
                    assertThat(entity.getId())
                            .isEqualTo(pointRuleId);
                    assertThat(entity.getMerchantId())
                            .isEqualTo(merchantId);
                    assertThat(entity.getPointsPerCurrencyUnit())
                            .isEqualByComparingTo("1.5000");
                    assertThat(entity.getRoundingMethod())
                            .isEqualTo((short) 1);
                    assertThat(entity.isActive())
                            .isTrue();
                });
    }

    @Test
    void shouldFindPointRulesByMerchantId() {
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);

        repository.save(createPointRule(
                UUID.randomUUID(),
                merchantId,
                new BigDecimal("1.0000"),
                (short) 0
        ));

        repository.save(createPointRule(
                UUID.randomUUID(),
                merchantId,
                new BigDecimal("1.5000"),
                (short) 1
        ));

        List<PointRuleEntity> result =
                repository.findByMerchantId(merchantId);

        assertThat(result)
                .hasSize(2)
                .extracting(PointRuleEntity::getMerchantId)
                .containsOnly(merchantId);
    }

    @Test
    void shouldReturnEmptyWhenMerchantHasNoPointRules() {
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);

        List<PointRuleEntity> result =
                repository.findByMerchantId(merchantId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnPointRulesFromAnotherMerchant() {
        UUID merchantId1 = UUID.randomUUID();
        UUID merchantId2 = UUID.randomUUID();

        insertMerchant(merchantId1);
        insertMerchant(merchantId2);

        repository.save(createPointRule(
                UUID.randomUUID(),
                merchantId1,
                new BigDecimal("1.0000"),
                (short) 0
        ));

        repository.save(createPointRule(
                UUID.randomUUID(),
                merchantId2,
                new BigDecimal("2.0000"),
                (short) 2
        ));

        List<PointRuleEntity> result =
                repository.findByMerchantId(merchantId1);

        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(entity -> {
                    assertThat(entity.getMerchantId())
                            .isEqualTo(merchantId1);
                    assertThat(entity.getPointsPerCurrencyUnit())
                            .isEqualByComparingTo("1.0000");
                });
    }

    private PointRuleEntity createPointRule(
            UUID id,
            UUID merchantId,
            BigDecimal pointsPerCurrencyUnit,
            short roundingMethod
    ) {
        return new PointRuleEntity(
                id,
                merchantId,
                pointsPerCurrencyUnit,
                roundingMethod,
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

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