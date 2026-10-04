package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.models.loyalties.rewards.PointRuleResponse;
import com.fidely.backend.api.dtos.models.loyalties.rewards.UpdatePointRuleRequest;
import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.domain.models.users.MerchantManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private IUserRepository userRepository;

    @Mock
    private Authentication authentication;

    private PointRuleController controller;

    private UUID userId;
    private UUID merchantId;
    private MerchantManager merchantManager;

    @BeforeEach
    void setUp() {
        controller = new PointRuleController(
                pointRuleService,
                userRepository
        );

        userId = UUID.randomUUID();
        merchantId = UUID.randomUUID();

        merchantManager = new MerchantManager(
                userId,
                merchantId,
                "manager@fidely.com",
                "John",
                "Doe",
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(authentication.getPrincipal())
                .thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));
    }

    @Test
    void shouldGetCurrentPointRule() {
        PointRule pointRule = createPointRule();

        when(pointRuleService.getValidPointRule(
                eq(merchantId),
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
                .getValidPointRule(
                        eq(merchantId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldReturnNotFoundWhenNoCurrentPointRuleExists() {
        when(pointRuleService.getValidPointRule(
                eq(merchantId),
                any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        var response = controller.getCurrentPointRule(authentication);

        assertThat(response.getStatusCode().value())
                .isEqualTo(404);

        assertThat(response.getBody())
                .isNull();

        verify(pointRuleService)
                .getValidPointRule(
                        eq(merchantId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldUpdatePointRule() {
        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        var request =
                new UpdatePointRuleRequest(
                        new BigDecimal("1.5000"),
                        RoundingMethod.ROUND,
                        true,
                        validFrom,
                        null
                );

        PointRule savedPointRule = new PointRule(
                UUID.randomUUID(),
                request.pointsPerCurrencyUnit(),
                request.roundingMethod(),
                request.active(),
                request.validFrom(),
                request.validTo(),
                LocalDateTime.now()
        );

        when(pointRuleService.createPointRule(
                any(PointRule.class),
                eq(merchantId)
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
                        any(PointRule.class),
                        eq(merchantId)
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