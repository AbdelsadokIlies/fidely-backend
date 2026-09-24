package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IRefreshSessionService;
import com.fidely.backend.application.port.out.IRefreshSessionRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.security.IRefreshTokenGenerator;
import com.fidely.backend.application.port.out.security.ITokenHasher;
import com.fidely.backend.domain.models.auth.RefreshSession;
import com.fidely.backend.domain.models.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTest {

    @Mock
    private IRefreshSessionRepository refreshSessionRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private ITokenHasher refreshTokenHasher;

    @Mock
    private User user;

    private RefreshSessionService service;

    @BeforeEach
    void setUp() {
        service = new RefreshSessionService(
                refreshSessionRepository,
                userRepository,
                refreshTokenGenerator,
                refreshTokenHasher
        );
    }

    // ---------------------------------------------------------
    // Create session
    // ---------------------------------------------------------

    @Test
    void shouldCreateRefreshSession() {
        UUID userId = UUID.randomUUID();

        String refreshToken = "refresh-token";
        String tokenHash = "hashed-refresh-token";

        RefreshSession savedSession =
                createSession(
                        UUID.randomUUID(),
                        userId,
                        UUID.randomUUID(),
                        tokenHash,
                        null
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(refreshTokenGenerator.generate())
                .thenReturn(refreshToken);

        when(refreshTokenHasher.hash(refreshToken))
                .thenReturn(tokenHash);

        when(refreshSessionRepository.save(any(RefreshSession.class)))
                .thenReturn(savedSession);

        IRefreshSessionService.RefreshTokenCreationResult result =
                service.createSession(userId);

        assertThat(result)
                .isNotNull();

        assertThat(result.refreshToken())
                .isEqualTo(refreshToken);

        assertThat(result.session())
                .isSameAs(savedSession);

        verify(userRepository)
                .findById(userId);

        verify(refreshTokenGenerator)
                .generate();

        verify(refreshTokenHasher)
                .hash(refreshToken);

        verify(refreshSessionRepository)
                .save(any(RefreshSession.class));
    }

    @Test
    void shouldRejectSessionCreationWhenUserIdIsNull() {
        assertThatThrownBy(
                () -> service.createSession(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID cannot be null");

        verifyNoInteractions(
                userRepository,
                refreshTokenGenerator,
                refreshTokenHasher,
                refreshSessionRepository
        );
    }

    @Test
    void shouldRejectSessionCreationWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.createSession(userId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + userId);

        verify(userRepository)
                .findById(userId);

        verifyNoInteractions(
                refreshTokenGenerator,
                refreshTokenHasher,
                refreshSessionRepository
        );
    }

    // ---------------------------------------------------------
    // Rotate
    // ---------------------------------------------------------

    @Test
    void shouldRotateRefreshToken() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();

        String oldRefreshToken = "old-refresh-token";
        String oldTokenHash = "old-token-hash";

        String newRefreshToken = "new-refresh-token";
        String newTokenHash = "new-token-hash";

        RefreshSession previousSession =
                createSession(
                        UUID.randomUUID(),
                        userId,
                        familyId,
                        oldTokenHash,
                        null
                );

        RefreshSession savedNewSession =
                createSession(
                        UUID.randomUUID(),
                        userId,
                        familyId,
                        newTokenHash,
                        null
                );

        when(refreshTokenHasher.hash(oldRefreshToken))
                .thenReturn(oldTokenHash);

        when(refreshSessionRepository.findByTokenHash(oldTokenHash))
                .thenReturn(Optional.of(previousSession));

        when(refreshTokenGenerator.generate())
                .thenReturn(newRefreshToken);

        when(refreshTokenHasher.hash(newRefreshToken))
                .thenReturn(newTokenHash);

        when(refreshSessionRepository.save(any(RefreshSession.class)))
                .thenReturn(savedNewSession);

        IRefreshSessionService.RefreshTokenRotationResult result =
                service.rotate(oldRefreshToken);

        assertThat(result)
                .isNotNull();

        assertThat(result.previousSession())
                .isSameAs(previousSession);

        assertThat(result.newSession())
                .isSameAs(savedNewSession);

        assertThat(result.refreshToken())
                .isEqualTo(newRefreshToken);

        assertThat(previousSession.isRevoked())
                .isTrue();

        assertThat(savedNewSession.isRevoked())
                .isFalse();

        verify(refreshTokenHasher)
                .hash(oldRefreshToken);

        verify(refreshSessionRepository)
                .findByTokenHash(oldTokenHash);

        verify(refreshTokenGenerator)
                .generate();

        verify(refreshTokenHasher)
                .hash(newRefreshToken);

        verify(refreshSessionRepository)
                .save(previousSession);

        verify(refreshSessionRepository, times(2))
                .save(any(RefreshSession.class));
    }

    @Test
    void shouldRejectBlankRefreshToken() {
        assertThatThrownBy(
                () -> service.rotate("   ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token cannot be null or blank");

        verifyNoInteractions(
                refreshSessionRepository,
                refreshTokenGenerator,
                refreshTokenHasher
        );
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        String refreshToken = "unknown-refresh-token";
        String tokenHash = "unknown-token-hash";

        when(refreshTokenHasher.hash(refreshToken))
                .thenReturn(tokenHash);

        when(refreshSessionRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.rotate(refreshToken)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenHasher)
                .hash(refreshToken);

        verify(refreshSessionRepository)
                .findByTokenHash(tokenHash);

        verifyNoInteractions(refreshTokenGenerator);
    }

    @Test
    void shouldRevokeFamilyWhenRefreshTokenHasAlreadyBeenUsed() {
        UUID familyId = UUID.randomUUID();

        String refreshToken = "already-used-token";
        String tokenHash = "already-used-hash";

        RefreshSession revokedSession =
                createRevokedSession(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        familyId,
                        tokenHash
                );

        when(refreshTokenHasher.hash(refreshToken))
                .thenReturn(tokenHash);

        when(refreshSessionRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(revokedSession));

        assertThatThrownBy(
                () -> service.rotate(refreshToken)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Refresh token has already been used");

        verify(refreshTokenHasher)
                .hash(refreshToken);

        verify(refreshSessionRepository)
                .findByTokenHash(tokenHash);

        verify(refreshSessionRepository)
                .revokeFamily(
                        eq(familyId),
                        any(LocalDateTime.class)
                );

        verifyNoInteractions(refreshTokenGenerator);
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();

        String refreshToken = "expired-token";
        String tokenHash = "expired-token-hash";

        LocalDateTime createdAt =
                LocalDateTime.now().minusDays(31);

        RefreshSession expiredSession =
                new RefreshSession(
                        sessionId,
                        userId,
                        familyId,
                        tokenHash,
                        createdAt.plusDays(30),
                        null,
                        createdAt
                );

        when(refreshTokenHasher.hash(refreshToken))
                .thenReturn(tokenHash);

        when(refreshSessionRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(expiredSession));

        assertThatThrownBy(
                () -> service.rotate(refreshToken)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Refresh token has expired");

        verify(refreshTokenHasher)
                .hash(refreshToken);

        verify(refreshSessionRepository)
                .findByTokenHash(tokenHash);

        verifyNoInteractions(refreshTokenGenerator);
    }

    // ---------------------------------------------------------
    // Revoke family
    // ---------------------------------------------------------

    @Test
    void shouldRevokeRefreshSessionFamily() {
        UUID familyId = UUID.randomUUID();

        service.revokeFamily(familyId);

        verify(refreshSessionRepository)
                .revokeFamily(
                        eq(familyId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldRejectNullFamilyIdWhenRevokingFamily() {
        assertThatThrownBy(
                () -> service.revokeFamily(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Family ID cannot be null");

        verifyNoInteractions(refreshSessionRepository);
    }

    @Test
    void shouldKeepSameUserAndFamilyWhenRotatingRefreshToken() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();

        String oldRefreshToken = "old-refresh-token";
        String oldTokenHash = "old-token-hash";

        String newRefreshToken = "new-refresh-token";
        String newTokenHash = "new-token-hash";

        RefreshSession previousSession =
                createSession(
                        UUID.randomUUID(),
                        userId,
                        familyId,
                        oldTokenHash,
                        null
                );

        when(refreshTokenHasher.hash(oldRefreshToken))
                .thenReturn(oldTokenHash);

        when(refreshSessionRepository.findByTokenHash(oldTokenHash))
                .thenReturn(Optional.of(previousSession));

        when(refreshTokenGenerator.generate())
                .thenReturn(newRefreshToken);

        when(refreshTokenHasher.hash(newRefreshToken))
                .thenReturn(newTokenHash);

        when(refreshSessionRepository.save(any(RefreshSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IRefreshSessionService.RefreshTokenRotationResult result =
                service.rotate(oldRefreshToken);

        RefreshSession newSession = result.newSession();

        assertThat(newSession.getUserId())
                .isEqualTo(userId);

        assertThat(newSession.getFamilyId())
                .isEqualTo(familyId);

        assertThat(newSession.getTokenHash())
                .isEqualTo(newTokenHash);

        assertThat(newSession.isRevoked())
                .isFalse();

        assertThat(previousSession.isRevoked())
                .isTrue();
    }

    @Test
    void shouldNotGenerateNewTokenWhenRefreshTokenIsUnknown() {
        String refreshToken = "unknown-refresh-token";
        String tokenHash = "unknown-token-hash";

        when(refreshTokenHasher.hash(refreshToken))
                .thenReturn(tokenHash);

        when(refreshSessionRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.rotate(refreshToken)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenGenerator, never())
                .generate();

        verify(refreshSessionRepository, never())
                .save(any(RefreshSession.class));

        verify(refreshSessionRepository, never())
                .revokeFamily(any(UUID.class), any(LocalDateTime.class));
    }

    @Test
    void shouldNotRotateWhenRefreshTokenIsExpired() {
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();

        String refreshToken = "expired-token";
        String tokenHash = "expired-token-hash";

        LocalDateTime createdAt =
                LocalDateTime.now().minusDays(31);

        RefreshSession expiredSession =
                new RefreshSession(
                        UUID.randomUUID(),
                        userId,
                        familyId,
                        tokenHash,
                        createdAt.plusDays(30),
                        null,
                        createdAt
                );

        when(refreshTokenHasher.hash(refreshToken))
                .thenReturn(tokenHash);

        when(refreshSessionRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(expiredSession));

        assertThatThrownBy(
                () -> service.rotate(refreshToken)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Refresh token has expired");

        verify(refreshTokenGenerator, never())
                .generate();

        verify(refreshSessionRepository, never())
                .save(any(RefreshSession.class));

        verify(refreshSessionRepository, never())
                .revokeFamily(any(UUID.class), any(LocalDateTime.class));
    }

    // ---------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------

    private RefreshSession createSession(
            UUID sessionId,
            UUID userId,
            UUID familyId,
            String tokenHash,
            LocalDateTime revokedAt
    ) {
        LocalDateTime createdAt =
                LocalDateTime.now();

        return new RefreshSession(
                sessionId,
                userId,
                familyId,
                tokenHash,
                createdAt.plusDays(30),
                revokedAt,
                createdAt
        );
    }

    private RefreshSession createRevokedSession(
            UUID sessionId,
            UUID userId,
            UUID familyId,
            String tokenHash
    ) {
        LocalDateTime createdAt =
                LocalDateTime.now();

        return new RefreshSession(
                sessionId,
                userId,
                familyId,
                tokenHash,
                createdAt.plusDays(30),
                createdAt.plusMinutes(1),
                createdAt
        );
    }
}