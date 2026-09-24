package com.fidely.backend.infrastructure.entities.mappers.auth;

import com.fidely.backend.domain.models.auth.RefreshSession;
import com.fidely.backend.infrastructure.entities.models.auth.RefreshSessionEntity;
import org.springframework.stereotype.Component;

/**
 * Convertit les sessions de rafraîchissement entre le domaine
 * et les entités de persistance.
 */
@Component
public class RefreshSessionMapper {

    /**
     * Convertit une entité de session en objet du domaine.
     *
     * @param entity entité de session à convertir
     * @return session de rafraîchissement du domaine
     * @throws IllegalArgumentException si l'entité est {@code null}
     */
    public RefreshSession toDomain(RefreshSessionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Refresh session entity cannot be null"
            );
        }

        return new RefreshSession(
                entity.getId(),
                entity.getUserId(),
                entity.getFamilyId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convertit une session du domaine en entité de persistance.
     *
     * @param session session de rafraîchissement à convertir
     * @return entité de session
     * @throws IllegalArgumentException si la session est {@code null}
     */
    public RefreshSessionEntity toEntity(RefreshSession session) {
        if (session == null) {
            throw new IllegalArgumentException(
                    "Refresh session cannot be null"
            );
        }

        return new RefreshSessionEntity(
                session.getId(),
                session.getUserId(),
                session.getFamilyId(),
                session.getTokenHash(),
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getCreatedAt()
        );
    }
}