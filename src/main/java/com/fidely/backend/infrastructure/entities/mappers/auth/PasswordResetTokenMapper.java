package com.fidely.backend.infrastructure.entities.mappers.auth;

import com.fidely.backend.application.port.out.IPasswordResetTokenRepository.PasswordResetTokenData;
import com.fidely.backend.infrastructure.entities.models.auth.PasswordResetTokenEntity;
import org.springframework.stereotype.Component;

/**
 * Convertit les données de token de réinitialisation entre
 * le domaine et l'entité de persistance.
 */
@Component
public class PasswordResetTokenMapper {

    /**
     * Convertit une entité de token en données utilisables par le domaine.
     *
     * @param entity entité de token à convertir
     * @return données du token
     * @throws IllegalArgumentException si l'entité est {@code null}
     */
    public PasswordResetTokenData toData(PasswordResetTokenEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Password reset token entity cannot be null"
            );
        }

        return new PasswordResetTokenData(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit les données d'un token en entité de persistance.
     *
     * @param data données du token à convertir
     * @return entité de token
     * @throws IllegalArgumentException si les données sont {@code null}
     */
    public PasswordResetTokenEntity toEntity(PasswordResetTokenData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Password reset token data cannot be null"
            );
        }

        return new PasswordResetTokenEntity(
                data.id(),
                data.userId(),
                data.tokenHash(),
                data.expiresAt(),
                data.usedAt(),
                data.createdAt()
        );
    }
}