package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.IPointRuleRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointRuleServiceTest {

    @Mock
    private IPointRuleRepository pointRuleRepository;

    private PointRuleService pointRuleService;

    @BeforeEach
    void setUp() {
        pointRuleService = new PointRuleService(
                pointRuleRepository
        );
    }

    @Test
    void shouldCreatePointRule() {
        UUID merchantId = UUID.randomUUID();
        PointRule pointRule = createPointRule();

        when(pointRuleRepository.save(pointRule, merchantId))
                .thenReturn(pointRule);

        PointRule result =
                pointRuleService.createPointRule(
                        pointRule,
                        merchantId
                );

        assertThat(result).isSameAs(pointRule);

        verify(pointRuleRepository)
                .save(pointRule, merchantId);
    }

    @Test
    void shouldGetPointRuleById() {
        UUID pointRuleId = UUID.randomUUID();
        PointRule pointRule = createPointRule(pointRuleId);

        when(pointRuleRepository.findById(pointRuleId))
                .thenReturn(Optional.of(pointRule));

        Optional<PointRule> result =
                pointRuleService.getPointRuleById(
                        pointRuleId
                );

        assertThat(result)
                .isPresent()
                .containsSame(pointRule);

        verify(pointRuleRepository)
                .findById(pointRuleId);
    }

    @Test
    void shouldReturnEmptyWhenPointRuleDoesNotExist() {
        UUID pointRuleId = UUID.randomUUID();

        when(pointRuleRepository.findById(pointRuleId))
                .thenReturn(Optional.empty());

        Optional<PointRule> result =
                pointRuleService.getPointRuleById(
                        pointRuleId
                );

        assertThat(result).isEmpty();

        verify(pointRuleRepository)
                .findById(pointRuleId);
    }

    @Test
    void shouldGetPointRulesByMerchant() {
        UUID merchantId = UUID.randomUUID();

        PointRule pointRule1 = createPointRule();
        PointRule pointRule2 = createPointRule();

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(pointRule1, pointRule2));

        List<PointRule> result =
                pointRuleService.getPointRulesByMerchant(
                        merchantId
                );

        assertThat(result)
                .containsExactly(pointRule1, pointRule2);

        verify(pointRuleRepository)
                .findByMerchantId(merchantId);
    }

    @Test
    void shouldReturnEmptyWhenMerchantHasNoPointRules() {
        UUID merchantId = UUID.randomUUID();

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        List<PointRule> result =
                pointRuleService.getPointRulesByMerchant(
                        merchantId
                );

        assertThat(result).isEmpty();

        verify(pointRuleRepository)
                .findByMerchantId(merchantId);
    }

    @Test
    void shouldGetValidPointRule() {
        UUID merchantId = UUID.randomUUID();

        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        LocalDateTime date =
                LocalDateTime.of(2026, 6, 15, 12, 0);

        PointRule pointRule =
                new PointRule(
                        UUID.randomUUID(),
                        new BigDecimal("1.0000"),
                        RoundingMethod.FLOOR,
                        true,
                        validFrom,
                        LocalDateTime.of(2026, 12, 31, 23, 59),
                        validFrom
                );

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(pointRule));

        Optional<PointRule> result =
                pointRuleService.getValidPointRule(
                        merchantId,
                        date
                );

        assertThat(result)
                .isPresent()
                .containsSame(pointRule);

        verify(pointRuleRepository)
                .findByMerchantId(merchantId);
    }

    @Test
    void shouldReturnEmptyWhenNoPointRuleIsValid() {
        UUID merchantId = UUID.randomUUID();

        PointRule pointRule =
                new PointRule(
                        UUID.randomUUID(),
                        new BigDecimal("1.0000"),
                        RoundingMethod.FLOOR,
                        true,
                        LocalDateTime.of(2026, 1, 1, 0, 0),
                        LocalDateTime.of(2026, 3, 31, 23, 59),
                        LocalDateTime.of(2026, 1, 1, 0, 0)
                );

        LocalDateTime date =
                LocalDateTime.of(2026, 6, 15, 12, 0);

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(pointRule));

        Optional<PointRule> result =
                pointRuleService.getValidPointRule(
                        merchantId,
                        date
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenPointRuleIsInactive() {
        UUID merchantId = UUID.randomUUID();

        PointRule pointRule =
                new PointRule(
                        UUID.randomUUID(),
                        new BigDecimal("1.0000"),
                        RoundingMethod.FLOOR,
                        false,
                        LocalDateTime.of(2026, 1, 1, 0, 0),
                        LocalDateTime.of(2026, 12, 31, 23, 59),
                        LocalDateTime.of(2026, 1, 1, 0, 0)
                );

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(pointRule));

        Optional<PointRule> result =
                pointRuleService.getValidPointRule(
                        merchantId,
                        LocalDateTime.of(2026, 6, 15, 12, 0)
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectNullDateWhenGettingValidPointRule() {
        UUID merchantId = UUID.randomUUID();

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(createPointRule()));

        assertThatThrownBy(() ->
                pointRuleService.getValidPointRule(
                        merchantId,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Date cannot be null");
    }

    @Test
    void shouldDeletePointRule() {
        UUID pointRuleId = UUID.randomUUID();

        pointRuleService.deletePointRule(pointRuleId);

        verify(pointRuleRepository)
                .deleteById(pointRuleId);
    }

    private PointRule createPointRule() {
        return createPointRule(UUID.randomUUID());
    }

    private PointRule createPointRule(UUID id) {
        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        return new PointRule(
                id,
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                LocalDateTime.of(2026, 12, 31, 23, 59),
                validFrom
        );
    }
}