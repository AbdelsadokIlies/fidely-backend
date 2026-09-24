package com.fidely.backend.infrastructure.entities.mappers.users;

import com.fidely.backend.domain.models.users.User;
import com.fidely.backend.infrastructure.entities.models.users.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de transformer les informations communes
 * d'un utilisateur entre le domaine et la persistance.
 *
 * <p>Ce mapper ne reconstruit pas les types concrets {@code Customer}
 * ou {@code MerchantManager}. Il ne manipule que les informations
 * communes présentes dans {@link UserEntity}.</p>
 *
 * <p>La reconstruction d'un utilisateur concret est réalisée par
 * les mappers spécialisés correspondants.</p>
 */
@Component
public class UserMapper {

    /**
     * Transforme les informations communes d'un utilisateur
     * du domaine vers une entité JPA.
     *
     * @param user utilisateur du domaine
     * @return entité JPA correspondante
     * @throws IllegalArgumentException si l'utilisateur est null
     */
    public UserEntity toEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        return new UserEntity(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEmailVerified(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}