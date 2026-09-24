package com.fidely.backend.api.dtos.models.auth;

import java.util.UUID;

/**
 * Réponse retournée après une authentification réussie.
 *
 * <p>Les tokens d'authentification ne sont pas exposés dans
 * cette réponse JSON. Ils sont transmis au client via des
 * cookies sécurisés gérés par la couche d'authentification.</p>
 *
 * @param id identifiant de l'utilisateur authentifié
 * @param email adresse e-mail de l'utilisateur
 * @param firstName prénom de l'utilisateur
 * @param lastName nom de l'utilisateur
 * @param emailVerified indique si l'adresse e-mail est vérifiée
 */
public record LoginResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean emailVerified
) {
}