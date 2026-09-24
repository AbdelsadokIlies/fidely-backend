package com.fidely.backend.infrastructure.mappers.auth;

import com.fidely.backend.domain.models.auth.RefreshSession;
import com.fidely.backend.infrastructure.entities.models.auth.RefreshSessionEntity;
import com.fidely.backend.infrastructure.entities.mappers.auth.RefreshSessionMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshSessionMapperTest {

    private final RefreshSessionMapper mapper = new RefreshSessionMapper();

    @Test
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);
        LocalDateTime revokedAt = createdAt.plusDays(1);

        RefreshSession session = new RefreshSession(
                id,
                userId,
                familyId,
                "hashed-token",
                expiresAt,
                revokedAt,
                createdAt
        );

        RefreshSessionEntity entity = mapper.toEntity(session);

        assertEquals(id, entity.getId());
        assertEquals(userId, entity.getUserId());
        assertEquals(familyId, entity.getFamilyId());
        assertEquals("hashed-token", entity.getTokenHash());
        assertEquals(expiresAt, entity.getExpiresAt());
        assertEquals(revokedAt, entity.getRevokedAt());
        assertEquals(createdAt, entity.getCreatedAt());
    }

    @Test
    void shouldMapDomainToEntityWithNoRevocation() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSession session = new RefreshSession(
                id,
                userId,
                familyId,
                "hashed-token",
                expiresAt,
                null,
                createdAt
        );

        RefreshSessionEntity entity = mapper.toEntity(session);

        assertEquals(id, entity.getId());
        assertEquals(userId, entity.getUserId());
        assertEquals(familyId, entity.getFamilyId());
        assertEquals("hashed-token", entity.getTokenHash());
        assertEquals(expiresAt, entity.getExpiresAt());
        assertNull(entity.getRevokedAt());
        assertEquals(createdAt, entity.getCreatedAt());
    }

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);
        LocalDateTime revokedAt = createdAt.plusDays(1);

        RefreshSessionEntity entity = new RefreshSessionEntity(
                id,
                userId,
                familyId,
                "hashed-token",
                expiresAt,
                revokedAt,
                createdAt
        );

        RefreshSession session = mapper.toDomain(entity);

        assertEquals(id, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(familyId, session.getFamilyId());
        assertEquals("hashed-token", session.getTokenHash());
        assertEquals(expiresAt, session.getExpiresAt());
        assertEquals(revokedAt, session.getRevokedAt());
        assertEquals(createdAt, session.getCreatedAt());
        assertTrue(session.isRevoked());
    }

    @Test
    void shouldMapEntityToDomainWithNoRevocation() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSessionEntity entity = new RefreshSessionEntity(
                id,
                userId,
                familyId,
                "hashed-token",
                expiresAt,
                null,
                createdAt
        );

        RefreshSession session = mapper.toDomain(entity);

        assertEquals(id, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(familyId, session.getFamilyId());
        assertEquals("hashed-token", session.getTokenHash());
        assertEquals(expiresAt, session.getExpiresAt());
        assertNull(session.getRevokedAt());
        assertFalse(session.isRevoked());
    }

    @Test
    void shouldRejectNullDomain() {
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toEntity(null)
        );
    }

    @Test
    void shouldRejectNullEntity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDomain(null)
        );
    }
}