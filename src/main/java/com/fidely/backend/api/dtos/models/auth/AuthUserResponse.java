package com.fidely.backend.api.dtos.models.auth;

import java.util.UUID;

/**
 * Représente les informations publiques d'un utilisateur
 * authentifié retournées par l'API.
 *
 * <p>Les informations sensibles liées à l'authentification,
 * notamment les mots de passe et les tokens, ne sont jamais
 * exposées dans cette réponse.</p>
 *
 * @param id identifiant de l'utilisateur
 * @param email adresse e-mail de l'utilisateur
 * @param firstName prénom de l'utilisateur
 * @param lastName nom de l'utilisateur
 * @param emailVerified indique si l'adresse e-mail est vérifiée
 */
public record AuthUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean emailVerified
) {
}