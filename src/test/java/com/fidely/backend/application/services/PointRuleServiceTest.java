package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.IPointRuleRepository;
import com.fidely.backend.application.port.out.IUserRepository;
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

    @Mock
    private IUserRepository userRepository;

    private PointRuleService pointRuleService;

    @BeforeEach
    void setUp() {
        pointRuleService = new PointRuleService(
                pointRuleRepository,
                userRepository
        );
    }

    @Test
    void shouldCreatePointRule() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime validTo =
                LocalDateTime.of(2026, 12, 31, 23, 59);

        when(userRepository.getMerchantId(userId))
                .thenReturn(merchantId);
        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        when(pointRuleRepository.save(
                any(PointRule.class),
                eq(merchantId)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        PointRule result = pointRuleService.createPointRule(
                userId,
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                validTo,
                validFrom
        );

        assertThat(result.getPointsPerCurrencyUnit())
                .isEqualByComparingTo("1.0000");
        assertThat(result.getRoundingMethod())
                .isEqualTo(RoundingMethod.FLOOR);
        assertThat(result.isActive()).isTrue();

        verify(userRepository).getMerchantId(userId);
        verify(pointRuleRepository).save(result, merchantId);
    }

    @Test
    void shouldGetPointRulesByMerchant() {
        UUID merchantId = UUID.randomUUID();
        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        PointRule pointRule1 = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                LocalDateTime.of(2026, 12, 31, 23, 59),
                validFrom
        );

        PointRule pointRule2 = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("2.0000"),
                RoundingMethod.ROUND,
                true,
                validFrom,
                LocalDateTime.of(2026, 12, 31, 23, 59),
                validFrom
        );

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(pointRule1, pointRule2));

        List<PointRule> result =
                pointRuleService.getPointRulesByMerchant(merchantId);

        assertThat(result)
                .containsExactly(pointRule1, pointRule2);

        verify(pointRuleRepository).findByMerchantId(merchantId);
    }

    @Test
    void shouldReturnEmptyWhenMerchantHasNoPointRules() {
        UUID merchantId = UUID.randomUUID();

        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        List<PointRule> result =
                pointRuleService.getPointRulesByMerchant(merchantId);

        assertThat(result).isEmpty();

        verify(pointRuleRepository).findByMerchantId(merchantId);
    }

    @Test
    void shouldGetValidPointRuleByUser() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime date =
                LocalDateTime.of(2026, 6, 15, 12, 0);

        PointRule pointRule = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                LocalDateTime.of(2026, 12, 31, 23, 59),
                validFrom
        );

        when(userRepository.getMerchantId(userId))
                .thenReturn(merchantId);
        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(pointRule));

        Optional<PointRule> result =
                pointRuleService.getValidPointRuleByUser(userId, date);

        assertThat(result)
                .isPresent()
                .containsSame(pointRule);

        verify(userRepository).getMerchantId(userId);
        verify(pointRuleRepository).findByMerchantId(merchantId);
    }

    @Test
    void shouldGetValidPointRuleByMerchant() {
        UUID merchantId = UUID.randomUUID();

        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime date =
                LocalDateTime.of(2026, 6, 15, 12, 0);

        PointRule pointRule = new PointRule(
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
                pointRuleService.getValidPointRuleByMerchant(
                        merchantId,
                        date
                );

        assertThat(result)
                .isPresent()
                .containsSame(pointRule);

        verify(pointRuleRepository).findByMerchantId(merchantId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldReturnEmptyWhenNoPointRuleIsValid() {
        UUID merchantId = UUID.randomUUID();

        PointRule pointRule = new PointRule(
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
                pointRuleService.getValidPointRuleByMerchant(
                        merchantId,
                        date
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenPointRuleIsInactive() {
        UUID merchantId = UUID.randomUUID();

        PointRule pointRule = new PointRule(
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
                pointRuleService.getValidPointRuleByMerchant(
                        merchantId,
                        LocalDateTime.of(2026, 6, 15, 12, 0)
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectNullMerchantIdWhenGettingRules() {
        assertThatThrownBy(
                () -> pointRuleService.getPointRulesByMerchant(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Merchant id cannot be null");

        verifyNoInteractions(pointRuleRepository);
    }

    @Test
    void shouldRejectNullMerchantIdWhenGettingValidRule() {
        assertThatThrownBy(
                () -> pointRuleService.getValidPointRuleByMerchant(
                        null,
                        LocalDateTime.of(2026, 6, 15, 12, 0)
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("MerchantId id cannot be null");

        verifyNoInteractions(pointRuleRepository);
    }

    @Test
    void shouldRejectNullDateWhenGettingValidRule() {
        assertThatThrownBy(
                () -> pointRuleService.getValidPointRuleByMerchant(
                        UUID.randomUUID(),
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Date cannot be null");

        verifyNoInteractions(pointRuleRepository);
    }

    @Test
    void shouldCreatePointRuleWhenNoActiveRuleExists() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime validTo =
                LocalDateTime.of(2026, 12, 31, 23, 59);

        when(userRepository.getMerchantId(userId))
                .thenReturn(merchantId);
        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of());

        when(pointRuleRepository.save(
                any(PointRule.class),
                eq(merchantId)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        PointRule result = pointRuleService.createPointRule(
                userId,
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                validTo,
                validFrom
        );

        assertThat(result.isActive()).isTrue();
        assertThat(result.getValidFrom()).isEqualTo(validFrom);
        assertThat(result.getValidTo()).isEqualTo(validTo);

        verify(pointRuleRepository).save(result, merchantId);
    }

    @Test
    void shouldCloseCurrentActiveRuleBeforeCreatingNewRule() {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        LocalDateTime oldValidFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime newValidFrom =
                LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime newValidTo =
                LocalDateTime.of(2026, 12, 31, 23, 59);

        PointRule currentRule = new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                oldValidFrom,
                newValidTo,
                oldValidFrom
        );

        when(userRepository.getMerchantId(userId))
                .thenReturn(merchantId);
        when(pointRuleRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(currentRule));

        when(pointRuleRepository.save(
                any(PointRule.class),
                eq(merchantId)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        PointRule result = pointRuleService.createPointRule(
                userId,
                new BigDecimal("2.0000"),
                RoundingMethod.ROUND,
                true,
                newValidFrom,
                null,
                newValidFrom
        );

        assertThat(result.getPointsPerCurrencyUnit())
                .isEqualByComparingTo("2.0000");
        assertThat(result.getRoundingMethod())
                .isEqualTo(RoundingMethod.ROUND);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getValidFrom()).isEqualTo(newValidFrom);

        ArgumentCaptor<PointRule> captor =
                ArgumentCaptor.forClass(PointRule.class);

        verify(pointRuleRepository, times(2))
                .save(captor.capture(), eq(merchantId));

        List<PointRule> savedRules = captor.getAllValues();

        PointRule closedRule = savedRules.get(0);
        PointRule newRule = savedRules.get(1);

        assertThat(closedRule.getId())
                .isEqualTo(currentRule.getId());
        assertThat(closedRule.isActive()).isFalse();
        assertThat(closedRule.getValidFrom()).isEqualTo(oldValidFrom);
        assertThat(closedRule.getValidTo()).isEqualTo(newValidFrom);

        assertThat(newRule.getId()).isNotEqualTo(currentRule.getId());
        assertThat(newRule.getPointsPerCurrencyUnit())
                .isEqualByComparingTo("2.0000");
        assertThat(newRule.getRoundingMethod())
                .isEqualTo(RoundingMethod.ROUND);
        assertThat(newRule.isActive()).isTrue();
        assertThat(newRule.getValidFrom()).isEqualTo(newValidFrom);
        assertThat(newRule.getValidTo()).isNull();
    }
}