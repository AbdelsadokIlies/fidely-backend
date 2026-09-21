package com.fidely.backend.infrastructure.mappers.Rewards;

import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.infrastructure.entities.loyalties.Rewards.PointRuleEntity;
import com.fidely.backend.infrastructure.mappers.loyalties.Rewards.PointRuleMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointRuleMapperTest {

    private final PointRuleMapper mapper = new PointRuleMapper();

    @Test
    void shouldMapEntityToDomainWithFloor() {
        UUID id = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime validTo =
                LocalDateTime.of(2026, 12, 31, 23, 59);
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        PointRuleEntity entity = new PointRuleEntity(
                id,
                merchantId,
                new BigDecimal("1.5000"),
                (short) 0,
                true,
                validFrom,
                validTo,
                createdAt
        );

        PointRule result = mapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getPointsPerCurrencyUnit())
                .isEqualByComparingTo("1.5000");
        assertThat(result.getRoundingMethod())
                .isEqualTo(RoundingMethod.FLOOR);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getValidFrom()).isEqualTo(validFrom);
        assertThat(result.getValidTo()).isEqualTo(validTo);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapEntityToDomainWithRound() {
        PointRuleEntity entity = createEntity((short) 1);

        PointRule result = mapper.toDomain(entity);

        assertThat(result.getRoundingMethod())
                .isEqualTo(RoundingMethod.ROUND);
    }

    @Test
    void shouldMapEntityToDomainWithCeil() {
        PointRuleEntity entity = createEntity((short) 2);

        PointRule result = mapper.toDomain(entity);

        assertThat(result.getRoundingMethod())
                .isEqualTo(RoundingMethod.CEIL);
    }

    @Test
    void shouldRejectUnknownRoundingMethodValue() {
        PointRuleEntity entity = createEntity((short) 99);

        assertThatThrownBy(() -> mapper.toDomain(entity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown rounding method value: 99");
    }

    @Test
    void shouldMapDomainToEntityWithFloor() {
        UUID merchantId = UUID.randomUUID();

        PointRule domain = createDomain(RoundingMethod.FLOOR);

        PointRuleEntity result =
                mapper.toEntity(domain, merchantId);

        assertThat(result.getId())
                .isEqualTo(domain.getId());
        assertThat(result.getMerchantId())
                .isEqualTo(merchantId);
        assertThat(result.getPointsPerCurrencyUnit())
                .isEqualByComparingTo(
                        domain.getPointsPerCurrencyUnit()
                );
        assertThat(result.getRoundingMethod())
                .isEqualTo((short) 0);
        assertThat(result.isActive())
                .isEqualTo(domain.isActive());
        assertThat(result.getValidFrom())
                .isEqualTo(domain.getValidFrom());
        assertThat(result.getValidTo())
                .isEqualTo(domain.getValidTo());
        assertThat(result.getCreatedAt())
                .isEqualTo(domain.getCreatedAt());
    }

    @Test
    void shouldMapDomainToEntityWithRound() {
        UUID merchantId = UUID.randomUUID();

        PointRule domain = createDomain(RoundingMethod.ROUND);

        PointRuleEntity result =
                mapper.toEntity(domain, merchantId);

        assertThat(result.getRoundingMethod())
                .isEqualTo((short) 1);
    }

    @Test
    void shouldMapDomainToEntityWithCeil() {
        UUID merchantId = UUID.randomUUID();

        PointRule domain = createDomain(RoundingMethod.CEIL);

        PointRuleEntity result =
                mapper.toEntity(domain, merchantId);

        assertThat(result.getRoundingMethod())
                .isEqualTo((short) 2);
    }

    @Test
    void shouldMapNullValidTo() {
        UUID merchantId = UUID.randomUUID();

        PointRule domain = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.5000"),
                RoundingMethod.ROUND,
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null,
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );

        PointRuleEntity result =
                mapper.toEntity(domain, merchantId);

        assertThat(result.getValidTo()).isNull();
    }

    private PointRuleEntity createEntity(short roundingMethod) {
        return new PointRuleEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1.5000"),
                roundingMethod,
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }

    private PointRule createDomain(RoundingMethod roundingMethod) {
        return new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.5000"),
                roundingMethod,
                true,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
    }
}