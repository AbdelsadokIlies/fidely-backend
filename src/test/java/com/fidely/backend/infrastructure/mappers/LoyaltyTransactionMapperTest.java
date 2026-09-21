package com.fidely.backend.infrastructure.mappers;

import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyTransactionEntity;
import com.fidely.backend.infrastructure.mappers.loyalties.LoyaltyTransactionMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoyaltyTransactionMapperTest {

    private final LoyaltyTransactionMapper mapper =
            new LoyaltyTransactionMapper();

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LoyaltyTransactionEntity entity =
                new LoyaltyTransactionEntity(
                        id,
                        loyaltyId,
                        ticketId,
                        100,
                        "Purchase",
                        createdAt
                );

        LoyaltyTransaction domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getLoyaltyId()).isEqualTo(loyaltyId);
        assertThat(domain.getTicketId()).isEqualTo(ticketId);
        assertThat(domain.getPoints()).isEqualTo(100);
        assertThat(domain.getDescription()).isEqualTo("Purchase");
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LoyaltyTransaction domain =
                new LoyaltyTransaction(
                        id,
                        loyaltyId,
                        ticketId,
                        100,
                        "Purchase",
                        createdAt
                );

        LoyaltyTransactionEntity entity =
                mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getLoyaltyId()).isEqualTo(loyaltyId);
        assertThat(entity.getTicketId()).isEqualTo(ticketId);
        assertThat(entity.getPoints()).isEqualTo(100);
        assertThat(entity.getDescription()).isEqualTo("Purchase");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapEntityToDomainWithNullTicketId() {
        UUID id = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LoyaltyTransactionEntity entity =
                new LoyaltyTransactionEntity(
                        id,
                        loyaltyId,
                        null,
                        50,
                        "Bonus",
                        createdAt
                );

        LoyaltyTransaction domain = mapper.toDomain(entity);

        assertThat(domain.getTicketId()).isNull();
    }

    @Test
    void shouldMapDomainToEntityWithNullTicketId() {
        UUID id = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LoyaltyTransaction domain =
                new LoyaltyTransaction(
                        id,
                        loyaltyId,
                        null,
                        50,
                        "Bonus",
                        createdAt
                );

        LoyaltyTransactionEntity entity =
                mapper.toEntity(domain);

        assertThat(entity.getTicketId()).isNull();
    }
}