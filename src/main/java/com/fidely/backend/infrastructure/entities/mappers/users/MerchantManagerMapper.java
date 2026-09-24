package com.fidely.backend.infrastructure.entities.mappers.users;

import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.infrastructure.entities.models.users.MerchantManagerEntity;
import com.fidely.backend.infrastructure.entities.models.users.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de transformer un gestionnaire de marchand
 * entre le domaine et les entités de persistance.
 *
 * <p>Les informations communes du compte utilisateur sont stockées
 * dans {@link UserEntity}, tandis que l'association au marchand
 * est stockée dans {@link MerchantManagerEntity}.</p>
 *
 * <p>Ce mapper ne réalise aucun accès à la persistance.</p>
 */
@Component
public class MerchantManagerMapper {

    /**
     * Transforme un gestionnaire de marchand du domaine
     * vers son entité utilisateur JPA.
     *
     * @param merchantManager gestionnaire du domaine
     * @return entité utilisateur correspondante
     * @throws IllegalArgumentException si le gestionnaire est null
     */
    public UserEntity toUserEntity(
            MerchantManager merchantManager
    ) {
        if (merchantManager == null) {
            throw new IllegalArgumentException(
                    "Merchant manager cannot be null"
            );
        }

        return new UserEntity(
                merchantManager.getId(),
                merchantManager.getEmail(),
                merchantManager.getFirstName(),
                merchantManager.getLastName(),
                merchantManager.isEmailVerified(),
                merchantManager.isActive(),
                merchantManager.getCreatedAt(),
                merchantManager.getUpdatedAt()
        );
    }

    /**
     * Transforme les informations spécifiques d'un gestionnaire
     * vers son entité JPA.
     *
     * @param merchantManager gestionnaire du domaine
     * @return entité du profil gestionnaire
     * @throws IllegalArgumentException si le gestionnaire est null
     */
    public MerchantManagerEntity toEntity(
            MerchantManager merchantManager
    ) {
        if (merchantManager == null) {
            throw new IllegalArgumentException(
                    "Merchant manager cannot be null"
            );
        }

        return new MerchantManagerEntity(
                merchantManager.getId(),
                merchantManager.getMerchantId()
        );
    }

    /**
     * Reconstruit un gestionnaire de marchand du domaine
     * à partir de ses entités JPA.
     *
     * @param userEntity entité contenant les informations communes
     * @param merchantManagerEntity entité contenant le profil gestionnaire
     * @return gestionnaire de marchand du domaine
     * @throws IllegalArgumentException si l'une des entités est null
     */
    public MerchantManager toDomain(
            UserEntity userEntity,
            MerchantManagerEntity merchantManagerEntity
    ) {
        if (userEntity == null) {
            throw new IllegalArgumentException(
                    "User entity cannot be null"
            );
        }

        if (merchantManagerEntity == null) {
            throw new IllegalArgumentException(
                    "Merchant manager entity cannot be null"
            );
        }

        return new MerchantManager(
                userEntity.getId(),
                merchantManagerEntity.getMerchantId(),
                userEntity.getEmail(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.isEmailVerified(),
                userEntity.isActive(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt()
        );
    }
}