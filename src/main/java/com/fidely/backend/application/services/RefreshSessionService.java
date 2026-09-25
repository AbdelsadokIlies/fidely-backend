package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IRefreshSessionService;
import com.fidely.backend.application.port.out.IRefreshSessionRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.security.IRefreshTokenGenerator;
import com.fidely.backend.application.port.out.security.ITokenHasher;
import com.fidely.backend.domain.models.auth.RefreshSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service applicatif responsable de la gestion des sessions
 * de refresh token.
 *
 * <p>Ce service gère la création et la rotation des refresh
 * tokens ainsi que la révocation des familles de sessions.</p>
 *
 * <p>Les refresh tokens sont générés aléatoirement et ne sont
 * jamais persistés en clair. Seul leur hash est conservé dans
 * les sessions de refresh.</p>
 *
 * <p>Lors d'une rotation, l'ancien token est révoqué et un nouveau
 * token appartenant à la même famille est créé.</p>
 *
 * <p>La détection d'un refresh token déjà révoqué entraîne la
 * révocation de toute sa famille afin de limiter l'impact d'une
 * éventuelle réutilisation d'un token compromis.</p>
 */
@Service
public class RefreshSessionService implements IRefreshSessionService {

    /**
     * Durée de validité maximale d'un refresh token.
     */
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;

    private final IRefreshSessionRepository refreshSessionRepository;
    private final IUserRepository userRepository;
    private final IRefreshTokenGenerator refreshTokenGenerator;
    private final ITokenHasher refreshTokenHasher;

    /**
     * Construit le service de gestion des sessions de refresh.
     *
     * @param refreshSessionRepository repository des sessions de refresh
     * @param userRepository repository des utilisateurs
     * @param refreshTokenGenerator générateur de refresh tokens
     * @param refreshTokenHasher service de hachage des refresh tokens
     */
    public RefreshSessionService(
            IRefreshSessionRepository refreshSessionRepository,
            IUserRepository userRepository,
            IRefreshTokenGenerator refreshTokenGenerator,
            ITokenHasher refreshTokenHasher
    ) {
        this.refreshSessionRepository = refreshSessionRepository;
        this.userRepository = userRepository;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public RefreshTokenCreationResult createSession(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId
                ));

        LocalDateTime now = LocalDateTime.now();
        String refreshToken = refreshTokenGenerator.generate();
        String tokenHash = refreshTokenHasher.hash(refreshToken);

        RefreshSession session = new RefreshSession(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                tokenHash,
                now.plusDays(REFRESH_TOKEN_VALIDITY_DAYS),
                null,
                now
        );

        RefreshSession savedSession =
                refreshSessionRepository.save(session);

        return new RefreshTokenCreationResult(
                savedSession,
                refreshToken
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public RefreshTokenRotationResult rotate(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token cannot be null or blank"
            );
        }

        String tokenHash = refreshTokenHasher.hash(refreshToken);

        RefreshSession previousSession =
                refreshSessionRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid refresh token"
                        ));

        LocalDateTime now = LocalDateTime.now();

        if (previousSession.isRevoked()) {
            refreshSessionRepository.revokeFamily(
                    previousSession.getFamilyId(),
                    now
            );

            throw new IllegalStateException(
                    "Refresh token has already been used"
            );
        }

        if (previousSession.isExpiredAt(now)) {
            throw new IllegalStateException(
                    "Refresh token has expired"
            );
        }

        String newRefreshToken =
                refreshTokenGenerator.generate();

        String newTokenHash =
                refreshTokenHasher.hash(newRefreshToken);

        previousSession.revoke(now);
        refreshSessionRepository.save(previousSession);

        RefreshSession newSession = new RefreshSession(
                UUID.randomUUID(),
                previousSession.getUserId(),
                previousSession.getFamilyId(),
                newTokenHash,
                now.plusDays(REFRESH_TOKEN_VALIDITY_DAYS),
                null,
                now
        );

        RefreshSession savedNewSession =
                refreshSessionRepository.save(newSession);

        return new RefreshTokenRotationResult(
                previousSession,
                savedNewSession,
                newRefreshToken
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void revokeFamily(UUID familyId) {
        if (familyId == null) {
            throw new IllegalArgumentException(
                    "Family ID cannot be null"
            );
        }

        refreshSessionRepository.revokeFamily(
                familyId,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void revokeSession(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token cannot be null or blank"
            );
        }

        String tokenHash = refreshTokenHasher.hash(refreshToken);

        RefreshSession session =
                refreshSessionRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid refresh token"
                        ));

        refreshSessionRepository.revokeFamily(
                session.getFamilyId(),
                LocalDateTime.now()
        );
    }
}