package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.models.loyalties.rewards.PointRuleResponse;
import com.fidely.backend.api.dtos.models.loyalties.rewards.UpdatePointRuleRequest;
import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointRuleControllerTest {

    @Mock
    private IPointRuleService pointRuleService;

    @Mock
    private Authentication authentication;

    private PointRuleController controller;

    private UUID userId;

    @BeforeEach
    void setUp() {
        controller = new PointRuleController(pointRuleService);

        userId = UUID.randomUUID();

        when(authentication.getPrincipal())
                .thenReturn(userId);
    }

    @Test
    void shouldGetCurrentPointRule() {
        PointRule pointRule = createPointRule();

        when(pointRuleService.getValidPointRuleByUser(
                eq(userId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(pointRule));

        var response = controller.getCurrentPointRule(authentication);

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().id())
                .isEqualTo(pointRule.getId());

        assertThat(response.getBody().pointsPerCurrencyUnit())
                .isEqualByComparingTo(
                        pointRule.getPointsPerCurrencyUnit()
                );

        assertThat(response.getBody().roundingMethod())
                .isEqualTo(pointRule.getRoundingMethod());

        assertThat(response.getBody().active())
                .isEqualTo(pointRule.isActive());

        verify(pointRuleService)
                .getValidPointRuleByUser(
                        eq(userId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldReturnNotFoundWhenNoCurrentPointRuleExists() {
        when(pointRuleService.getValidPointRuleByUser(
                eq(userId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        var response = controller.getCurrentPointRule(authentication);

        assertThat(response.getStatusCode().value())
                .isEqualTo(404);

        assertThat(response.getBody())
                .isNull();

        verify(pointRuleService)
                .getValidPointRuleByUser(
                        eq(userId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldUpdatePointRule() {
        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        var request = new UpdatePointRuleRequest(
                new BigDecimal("1.5000"),
                RoundingMethod.ROUND,
                true,
                validFrom,
                null
        );

        UUID generatedRuleId = UUID.randomUUID();

        PointRule savedPointRule = new PointRule(
                generatedRuleId,
                request.pointsPerCurrencyUnit(),
                request.roundingMethod(),
                request.active(),
                request.validFrom(),
                request.validTo(),
                LocalDateTime.now()
        );

        when(pointRuleService.createPointRule(
                eq(userId),
                eq(request.pointsPerCurrencyUnit()),
                eq(request.roundingMethod()),
                eq(request.active()),
                eq(request.validFrom()),
                eq(request.validTo()),
                any(LocalDateTime.class)
        )).thenReturn(savedPointRule);

        var response = controller.updatePointRule(
                request,
                authentication
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        PointRuleResponse body = response.getBody();

        assertThat(body)
                .isNotNull();

        assertThat(body.id())
                .isEqualTo(savedPointRule.getId());

        assertThat(body.pointsPerCurrencyUnit())
                .isEqualByComparingTo("1.5000");

        assertThat(body.roundingMethod())
                .isEqualTo(RoundingMethod.ROUND);

        assertThat(body.active())
                .isTrue();

        assertThat(body.validFrom())
                .isEqualTo(validFrom);

        assertThat(body.validTo())
                .isNull();

        verify(pointRuleService)
                .createPointRule(
                        eq(userId),
                        eq(request.pointsPerCurrencyUnit()),
                        eq(request.roundingMethod()),
                        eq(request.active()),
                        eq(request.validFrom()),
                        eq(request.validTo()),
                        any(LocalDateTime.class)
                );
    }

    private PointRule createPointRule() {
        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        return new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                LocalDateTime.of(2026, 12, 31, 23, 59),
                validFrom
        );
    }
}