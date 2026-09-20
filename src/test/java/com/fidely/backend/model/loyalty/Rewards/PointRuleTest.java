package com.fidely.backend.model.loyalty.Rewards;

import com.fidely.backend.domain.model.Merchants.Merchant;
import com.fidely.backend.domain.model.loyalty.Rewards.PointRule;
import com.fidely.backend.domain.model.loyalty.Rewards.RoundingMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PointRuleTest {

    private final Merchant merchant = null;

    // ---------------------------------------------------------
    // calculatePoints()
    // ---------------------------------------------------------

    @Test
    void shouldCalculatePointsWithFloor() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1.5"),
                RoundingMethod.FLOOR
        );

        int result = pointRule.calculatePoints(
                new BigDecimal("10.20")
        );

        assertEquals(15, result);
    }

    @Test
    void shouldCalculatePointsWithRound() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1.5"),
                RoundingMethod.ROUND
        );

        int result = pointRule.calculatePoints(
                new BigDecimal("10.20")
        );

        assertEquals(15, result);
    }

    @Test
    void shouldCalculatePointsWithCeil() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1.5"),
                RoundingMethod.CEIL
        );

        int result = pointRule.calculatePoints(
                new BigDecimal("10.20")
        );

        assertEquals(16, result);
    }

    @Test
    void shouldRoundHalfUpWhenUsingRound() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND
        );

        int result = pointRule.calculatePoints(
                new BigDecimal("10.50")
        );

        assertEquals(11, result);
    }

    @Test
    void shouldReturnZeroPointsForZeroAmount() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1.5"),
                RoundingMethod.ROUND
        );

        int result = pointRule.calculatePoints(
                BigDecimal.ZERO
        );

        assertEquals(0, result);
    }

    @Test
    void shouldRejectNegativeAmount() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> pointRule.calculatePoints(
                        new BigDecimal("-10")
                )
        );
    }

    @Test
    void shouldRejectNullAmount() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> pointRule.calculatePoints(null)
        );
    }


    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    @Test
    void shouldRejectZeroPointsPerCurrencyUnit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createPointRule(
                        BigDecimal.ZERO,
                        RoundingMethod.ROUND
                )
        );
    }

    @Test
    void shouldRejectNegativePointsPerCurrencyUnit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createPointRule(
                        new BigDecimal("-1"),
                        RoundingMethod.ROUND
                )
        );
    }

    @Test
    void shouldRejectNullRoundingMethod() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createPointRule(
                        new BigDecimal("1"),
                        null
                )
        );
    }

    @Test
    void shouldRejectNullValidFrom() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PointRule(
                        UUID.randomUUID(),
                        merchant,
                        new BigDecimal("1"),
                        RoundingMethod.ROUND,
                        true,
                        null,
                        null,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectValidToBeforeValidFrom() {

        LocalDateTime validFrom = LocalDateTime.of(
                2026, 1, 1, 0, 0
        );

        LocalDateTime validTo = LocalDateTime.of(
                2025, 1, 1, 0, 0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PointRule(
                        UUID.randomUUID(),
                        merchant,
                        new BigDecimal("1"),
                        RoundingMethod.ROUND,
                        true,
                        validFrom,
                        validTo,
                        LocalDateTime.now()
                )
        );
    }


    // ---------------------------------------------------------
    // isValidAt()
    // ---------------------------------------------------------

    @Test
    void shouldBeValidWhenDateIsInsideValidityPeriod() {

        LocalDateTime validFrom = LocalDateTime.of(
                2026, 1, 1, 0, 0
        );

        LocalDateTime validTo = LocalDateTime.of(
                2026, 12, 31, 23, 59
        );

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND,
                validFrom,
                validTo,
                true
        );

        assertTrue(
                pointRule.isValidAt(
                        LocalDateTime.of(2026, 6, 15, 12, 0)
                )
        );
    }

    @Test
    void shouldNotBeValidWhenRuleIsInactive() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                false
        );

        assertFalse(
                pointRule.isValidAt(
                        LocalDateTime.of(2026, 6, 15, 12, 0)
                )
        );
    }

    @Test
    void shouldNotBeValidBeforeValidFrom() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                true
        );

        assertFalse(
                pointRule.isValidAt(
                        LocalDateTime.of(2026, 5, 31, 23, 59)
                )
        );
    }

    @Test
    void shouldNotBeValidAfterValidTo() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59),
                true
        );

        assertFalse(
                pointRule.isValidAt(
                        LocalDateTime.of(2026, 7, 1, 0, 0)
                )
        );
    }

    @Test
    void shouldBeValidWhenValidToIsNull() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null,
                true
        );

        assertTrue(
                pointRule.isValidAt(
                        LocalDateTime.of(2030, 1, 1, 0, 0)
                )
        );
    }

    @Test
    void shouldBeValidExactlyAtValidFrom() {

        LocalDateTime validFrom = LocalDateTime.of(
                2026, 1, 1, 0, 0
        );

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND,
                validFrom,
                null,
                true
        );

        assertTrue(
                pointRule.isValidAt(validFrom)
        );
    }

    @Test
    void shouldBeValidExactlyAtValidTo() {

        LocalDateTime validTo = LocalDateTime.of(
                2026, 12, 31, 23, 59
        );

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                validTo,
                true
        );

        assertTrue(
                pointRule.isValidAt(validTo)
        );
    }

    @Test
    void shouldRejectNullDate() {

        PointRule pointRule = createPointRule(
                new BigDecimal("1"),
                RoundingMethod.ROUND
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> pointRule.isValidAt(null)
        );
    }


    // ---------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------

    private PointRule createPointRule(
            BigDecimal pointsPerCurrencyUnit,
            RoundingMethod roundingMethod
    ) {
        return createPointRule(
                pointsPerCurrencyUnit,
                roundingMethod,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                true
        );
    }

    private PointRule createPointRule(
            BigDecimal pointsPerCurrencyUnit,
            RoundingMethod roundingMethod,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            boolean active
    ) {
        return new PointRule(
                UUID.randomUUID(),
                merchant,
                pointsPerCurrencyUnit,
                roundingMethod,
                active,
                validFrom,
                validTo,
                LocalDateTime.now()
        );
    }
}