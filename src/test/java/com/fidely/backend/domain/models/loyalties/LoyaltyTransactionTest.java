package com.fidely.backend.domain.models.loyalties;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoyaltyTransactionTest {

    private final UUID loyaltyId = UUID.randomUUID();
    private final UUID ticketId = UUID.randomUUID();

    @Test
    void shouldCreateTransactionWithPositivePoints() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        LoyaltyTransaction transaction = createTransaction(
                100,
                ticketId,
                createdAt
        );

        assertThat(transaction.getPoints()).isEqualTo(100);
        assertThat(transaction.getLoyaltyId()).isEqualTo(loyaltyId);
        assertThat(transaction.getTicketId()).isEqualTo(ticketId);
        assertThat(transaction.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldAllowNullTicketId() {
        LoyaltyTransaction transaction = createTransaction(
                50,
                null,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        assertThat(transaction.getTicketId()).isNull();
    }

    @Test
    void shouldRejectZeroPoints() {
        assertThatThrownBy(() -> createTransaction(
                0,
                ticketId,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Points must be positive");
    }

    @Test
    void shouldRejectNegativePoints() {
        assertThatThrownBy(() -> createTransaction(
                -100,
                ticketId,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Points must be positive");
    }

    @Test
    void shouldRejectNullLoyalty() {
        assertThatThrownBy(() -> new LoyaltyTransaction(
                UUID.randomUUID(),
                null,
                ticketId,
                100,
                "Purchase",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Loyalty cannot be null");
    }

    @Test
    void shouldRejectNullCreatedAt() {
        assertThatThrownBy(() -> createTransaction(
                100,
                ticketId,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Created at cannot be null");
    }

    @Test
    void shouldKeepDescription() {
        LoyaltyTransaction transaction = new LoyaltyTransaction(
                UUID.randomUUID(),
                loyaltyId,
                ticketId,
                100,
                "Purchase at restaurant",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        assertThat(transaction.getDescription())
                .isEqualTo("Purchase at restaurant");
    }

    @Test
    void shouldKeepId() {
        UUID id = UUID.randomUUID();

        LoyaltyTransaction transaction = new LoyaltyTransaction(
                id,
                loyaltyId,
                ticketId,
                100,
                "Purchase",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        assertThat(transaction.getId()).isEqualTo(id);
    }

    private LoyaltyTransaction createTransaction(
            int points,
            UUID ticketId,
            LocalDateTime createdAt
    ) {
        return new LoyaltyTransaction(
                UUID.randomUUID(),
                loyaltyId,
                ticketId,
                points,
                "Test transaction",
                createdAt
        );
    }
}