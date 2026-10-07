package com.fidely.backend.api.controllers.integrations;

import com.fidely.backend.api.controllers.PointRuleController;
import com.fidely.backend.application.port.in.IPointRuleService;
import com.fidely.backend.domain.models.loyalties.Rewards.PointRule;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests web du controller REST des règles d'attribution de points.
 */
@WebMvcTest(PointRuleController.class)
class PointRuleControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IPointRuleService pointRuleService;

    @Test
    void shouldGetCurrentPointRule() throws Exception {
        UUID userId = UUID.randomUUID();

        PointRule pointRule = createPointRule();

        when(pointRuleService.getValidPointRule(
                eq(userId),
                any(LocalDateTime.class)
        )).thenReturn(java.util.Optional.of(pointRule));

        mockMvc.perform(
                        get("/merchants/me/points-rule")
                                .principal(createAuthentication(userId))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.id")
                        .value(pointRule.getId().toString()))
                .andExpect(jsonPath("$.pointsPerCurrencyUnit")
                        .value(1.0))
                .andExpect(jsonPath("$.roundingMethod")
                        .value(RoundingMethod.FLOOR.name()))
                .andExpect(jsonPath("$.active")
                        .value(true))
                .andExpect(jsonPath("$.validFrom")
                        .value("2026-01-01T00:00:00"))
                .andExpect(jsonPath("$.validTo")
                        .value("2026-12-31T23:59:00"));
    }

    @Test
    void shouldReturnNotFoundWhenNoCurrentPointRuleExists()
            throws Exception {
        UUID userId = UUID.randomUUID();

        when(pointRuleService.getValidPointRule(
                eq(userId),
                any(LocalDateTime.class)
        )).thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                        get("/merchants/me/points-rule")
                                .principal(createAuthentication(userId))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPointRule() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        LocalDateTime validFrom =
                LocalDateTime.of(2026, 6, 1, 0, 0);

        PointRule savedPointRule = new PointRule(
                pointRuleId,
                new BigDecimal("2.0000"),
                RoundingMethod.ROUND,
                true,
                validFrom,
                null,
                validFrom
        );

        when(pointRuleService.createPointRule(
                eq(userId),
                eq(new BigDecimal("2.0000")),
                eq(RoundingMethod.ROUND),
                eq(true),
                eq(validFrom),
                eq(null),
                any(LocalDateTime.class)
        )).thenReturn(savedPointRule);

        mockMvc.perform(
                        put("/merchants/me/points-rule")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "pointsPerCurrencyUnit": 2.0000,
                                        "roundingMethod": "ROUND",
                                        "active": true,
                                        "validFrom": "2026-06-01T00:00:00",
                                        "validTo": null
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.id")
                        .value(pointRuleId.toString()))
                .andExpect(jsonPath("$.pointsPerCurrencyUnit")
                        .value(2.0))
                .andExpect(jsonPath("$.roundingMethod")
                        .value(RoundingMethod.ROUND.name()))
                .andExpect(jsonPath("$.active")
                        .value(true))
                .andExpect(jsonPath("$.validFrom")
                        .value("2026-06-01T00:00:00"))
                .andExpect(jsonPath("$.validTo")
                        .doesNotExist());

        verify(pointRuleService)
                .createPointRule(
                        eq(userId),
                        eq(new BigDecimal("2.0000")),
                        eq(RoundingMethod.ROUND),
                        eq(true),
                        eq(validFrom),
                        eq(null),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldRejectInvalidPointRuleRequest() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        put("/merchants/me/points-rule")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "pointsPerCurrencyUnit": null,
                                        "roundingMethod": null,
                                        "active": true,
                                        "validFrom": null,
                                        "validTo": null
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Crée une authentification simulée contenant l'identifiant
     * utilisateur attendu par le controller.
     *
     * @param userId identifiant de l'utilisateur
     * @return authentification simulée
     */
    private Authentication createAuthentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null
        );
    }

    /**
     * Crée une règle de points utilisée dans les tests.
     *
     * @return règle de points de test
     */
    private PointRule createPointRule() {
        LocalDateTime validFrom =
                LocalDateTime.of(2026, 1, 1, 0, 0);

        return new PointRule(
                UUID.randomUUID(),
                new BigDecimal("1.0000"),
                RoundingMethod.FLOOR,
                true,
                validFrom,
                LocalDateTime.of(
                        2026,
                        12,
                        31,
                        23,
                        59
                ),
                validFrom
        );
    }
}