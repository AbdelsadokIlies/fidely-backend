package com.fidely.backend.infrastructure.entities.mappers.auth;

import com.fidely.backend.domain.models.auth.AuthIdentity;
import com.fidely.backend.infrastructure.entities.models.auth.AuthIdentityEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de convertir les identités d'authentification
 * entre le modèle du domaine et l'entité JPA.
 *
 * <p>Ce mapper ne contient aucune logique d'accès aux données.
 * Il est limité à la conversion des objets entre les différentes
 * couches de l'application.</p>
 */
@Component
public class AuthIdentityMapper {

    /**
     * Convertit une identité du domaine en entité JPA.
     *
     * @param identity identité du domaine
     * @return entité JPA correspondante
     * @throws IllegalArgumentException si l'identité est nulle
     */
    public AuthIdentityEntity toEntity(AuthIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException(
                    "Auth identity cannot be null"
            );
        }

        return new AuthIdentityEntity(
                identity.getId(),
                identity.getUserId(),
                identity.getProvider(),
                identity.getProviderUserId(),
                identity.getPasswordHash(),
                identity.getCreatedAt()
        );
    }

    /**
     * Convertit une entité JPA en identité du domaine.
     *
     * @param entity entité JPA
     * @return identité du domaine correspondante
     * @throws IllegalArgumentException si l'entité est nulle
     */
    public AuthIdentity toDomain(AuthIdentityEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Auth identity entity cannot be null"
            );
        }

        return new AuthIdentity(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getProviderUserId(),
                entity.getPasswordHash(),
                entity.getCreatedAt()
        );
    }
}