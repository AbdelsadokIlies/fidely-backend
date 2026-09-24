package com.fidely.backend.domain.models.auth;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshSessionTest {

    @Test
    void shouldCreateRefreshSession() {
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

        assertEquals(id, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(familyId, session.getFamilyId());
        assertEquals("hashed-token", session.getTokenHash());
        assertEquals(expiresAt, session.getExpiresAt());
        assertNull(session.getRevokedAt());
        assertEquals(createdAt, session.getCreatedAt());
        assertFalse(session.isRevoked());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshSession(
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "hashed-token",
                        LocalDateTime.now().plusDays(30),
                        null,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshSession(
                        UUID.randomUUID(),
                        null,
                        UUID.randomUUID(),
                        "hashed-token",
                        LocalDateTime.now().plusDays(30),
                        null,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectNullFamilyId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshSession(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "hashed-token",
                        LocalDateTime.now().plusDays(30),
                        null,
                        LocalDateTime.now()
                )
        );
    }

    @Test
    void shouldRejectBlankTokenHash() {
        LocalDateTime createdAt = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshSession(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        " ",
                        createdAt.plusDays(30),
                        null,
                        createdAt
                )
        );
    }

    @Test
    void shouldRejectExpirationBeforeOrAtCreation() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshSession(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "hashed-token",
                        createdAt,
                        null,
                        createdAt
                )
        );
    }

    @Test
    void shouldRejectRevocationBeforeCreation() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> new RefreshSession(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "hashed-token",
                        createdAt.plusDays(30),
                        createdAt.minusMinutes(1),
                        createdAt
                )
        );
    }

    @Test
    void shouldDetectExpiration() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSession session = createSession(createdAt, expiresAt);

        assertFalse(session.isExpiredAt(expiresAt.minusSeconds(1)));
        assertTrue(session.isExpiredAt(expiresAt));
        assertTrue(session.isExpiredAt(expiresAt.plusSeconds(1)));
    }

    @Test
    void shouldBeUsableBeforeExpiration() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSession session = createSession(createdAt, expiresAt);

        assertTrue(session.isUsableAt(expiresAt.minusSeconds(1)));
    }

    @Test
    void shouldNotBeUsableAfterExpiration() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSession session = createSession(createdAt, expiresAt);

        assertFalse(session.isUsableAt(expiresAt));
    }

    @Test
    void shouldRevokeSession() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);
        LocalDateTime revokedAt = createdAt.plusDays(1);

        RefreshSession session = createSession(createdAt, expiresAt);

        session.revoke(revokedAt);

        assertTrue(session.isRevoked());
        assertEquals(revokedAt, session.getRevokedAt());
        assertFalse(session.isUsableAt(revokedAt));
    }

    @Test
    void shouldRejectSecondRevocation() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);
        LocalDateTime revokedAt = createdAt.plusDays(1);

        RefreshSession session = createSession(createdAt, expiresAt);

        session.revoke(revokedAt);

        assertThrows(
                IllegalStateException.class,
                () -> session.revoke(revokedAt.plusMinutes(1))
        );
    }

    @Test
    void shouldRejectNullReferenceDate() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSession session = createSession(createdAt, expiresAt);

        assertThrows(
                IllegalArgumentException.class,
                () -> session.isExpiredAt(null)
        );
    }

    @Test
    void shouldRejectNullRevocationDate() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSession session = createSession(createdAt, expiresAt);

        assertThrows(
                IllegalArgumentException.class,
                () -> session.revoke(null)
        );
    }

    @Test
    void shouldRejectRevocationBeforeCreationDate() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = createdAt.plusDays(30);

        RefreshSession session = createSession(createdAt, expiresAt);

        assertThrows(
                IllegalArgumentException.class,
                () -> session.revoke(createdAt.minusSeconds(1))
        );
    }

    private RefreshSession createSession(
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        return new RefreshSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "hashed-token",
                expiresAt,
                null,
                createdAt
        );
    }
}