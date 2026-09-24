package com.fidely.backend.infrastructure.mappers.loyalties;

import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.infrastructure.entities.models.loyalties.LoyaltyEntity;
import com.fidely.backend.infrastructure.entities.mappers.loyalties.LoyaltyMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoyaltyMapperTest {

    private final LoyaltyMapper mapper = new LoyaltyMapper();

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 1, 2, 12, 0);

        LoyaltyEntity entity = new LoyaltyEntity(
                id,
                customerId,
                merchantId,
                150,
                createdAt,
                updatedAt
        );

        Loyalty result = mapper.toDomain(entity);

        assertThat(result.getId())
                .isEqualTo(id);

        assertThat(result.getCustomerId())
                .isEqualTo(customerId);

        assertThat(result.getMerchantId())
                .isEqualTo(merchantId);

        assertThat(result.getPointsBalance())
                .isEqualTo(150);

        assertThat(result.getCreatedAt())
                .isEqualTo(createdAt);

        assertThat(result.getUpdatedAt())
                .isEqualTo(updatedAt);
    }

    @Test
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 1, 2, 12, 0);

        Loyalty loyalty = new Loyalty(
                id,
                customerId,
                merchantId,
                200,
                createdAt,
                updatedAt
        );

        LoyaltyEntity result = mapper.toEntity(loyalty);

        assertThat(result.getId())
                .isEqualTo(id);

        assertThat(result.getCustomerId())
                .isEqualTo(customerId);

        assertThat(result.getMerchantId())
                .isEqualTo(merchantId);

        assertThat(result.getPointsBalance())
                .isEqualTo(200);

        assertThat(result.getCreatedAt())
                .isEqualTo(createdAt);

        assertThat(result.getUpdatedAt())
                .isEqualTo(updatedAt);
    }
}