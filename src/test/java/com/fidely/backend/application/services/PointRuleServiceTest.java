package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.IPointRuleRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    void shouldCreatePointRuleWhenNoActiveRuleExists() {
        UUID merchantId = UUID.randomUUID();

        PointRule pointRule = createPointRule();

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        when(pointRuleRepository.save(pointRule, merchantId))
                .thenReturn(pointRule);

        PointRule result = pointRuleService.createPointRule(
                pointRule,
                merchantId
        );

        assertThat(result).isSameAs(pointRule);

        verify(pointRuleRepository)
                .save(pointRule, merchantId);
    }

    @Test
    void shouldCloseCurrentActiveRuleBeforeCreatingNewRule() {
        UUID merchantId = UUID.randomUUID();

        LocalDateTime oldValidFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        LocalDateTime newValidFrom =
                LocalDateTime.of(2026, 6, 1, 0, 0);

        PointRule currentRule = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                oldValidFrom,
                LocalDateTime.of(2026, 12, 31, 23, 59),
                oldValidFrom
        );

        PointRule newRule = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("2.0000"),
                RoundingMethod.ROUND,
                true,
                newValidFrom,
                null,
                newValidFrom
        );

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(currentRule));

        when(pointRuleRepository.save(
                any(PointRule.class),
                eq(merchantId)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        PointRule result = pointRuleService.createPointRule(
                newRule,
                merchantId
        );

        assertThat(result).isSameAs(newRule);

        ArgumentCaptor<PointRule> captor =
                ArgumentCaptor.forClass(PointRule.class);

        verify(pointRuleRepository, times(2))
                .save(captor.capture(), eq(merchantId));

        List<PointRule> savedRules = captor.getAllValues();

        PointRule closedRule = savedRules.get(0);

        assertThat(closedRule.getId())
                .isEqualTo(currentRule.getId());

        assertThat(closedRule.isActive())
                .isFalse();

        assertThat(closedRule.getValidFrom())
                .isEqualTo(oldValidFrom);

        assertThat(closedRule.getValidTo())
                .isEqualTo(newValidFrom);

        assertThat(savedRules.get(1))
                .isSameAs(newRule);
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