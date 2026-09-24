package com.fidely.backend.api.dtos.mappers.auth;

import com.fidely.backend.api.dtos.models.auth.LoginResponse;
import com.fidely.backend.domain.models.users.User;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de convertir un utilisateur authentifié
 * en réponse API de connexion.
 *
 * <p>Ce mapper ne contient aucune logique métier et n'effectue
 * aucun accès aux repositories.</p>
 */
@Component
public class LoginResponseMapper {

    /**
     * Convertit un utilisateur authentifié en réponse API.
     *
     * @param user utilisateur authentifié
     * @return réponse API de connexion
     */
    public LoginResponse toResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEmailVerified()
        );
    }
}