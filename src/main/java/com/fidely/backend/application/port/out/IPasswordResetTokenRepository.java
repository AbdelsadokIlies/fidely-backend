package com.fidely.backend.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie permettant de gérer les tokens de réinitialisation de mot de passe.
 */
public interface IPasswordResetTokenRepository {

    Optional<PasswordResetTokenData> findByTokenHash(String tokenHash);

    void invalidateByUserId(UUID userId, LocalDateTime usedAt);

    void markAsUsed(UUID id, LocalDateTime usedAt);

    PasswordResetTokenData save(PasswordResetTokenData token);

    void deleteById(UUID id);

    /**
     * Données nécessaires à la gestion d'un token de réinitialisation.
     *
     * @param id identifiant du token
     * @param userId identifiant de l'utilisateur associé
     * @param tokenHash hash du token
     * @param expiresAt date d'expiration du token
     * @param usedAt date d'utilisation du token
     * @param createdAt date de création du token
     */
    record PasswordResetTokenData(
            UUID id,
            UUID userId,
            String tokenHash,
            LocalDateTime expiresAt,
            LocalDateTime usedAt,
            LocalDateTime createdAt
    ) {
    }
}