package com.fidely.backend.api.dtos.mappers.auth;

import com.fidely.backend.api.dtos.models.auth.AuthUserResponse;
import com.fidely.backend.domain.models.users.User;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de convertir un utilisateur du domaine
 * en réponse API d'authentification.
 *
 * <p>Ce mapper ne contient aucune logique métier et n'effectue
 * aucun accès aux repositories.</p>
 */
@Component
public class AuthUserResponseMapper {

    /**
     * Convertit un utilisateur du domaine en réponse API.
     *
     * @param user utilisateur à convertir
     * @return réponse API correspondante
     */
    public AuthUserResponse toResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEmailVerified()
        );
    }
}