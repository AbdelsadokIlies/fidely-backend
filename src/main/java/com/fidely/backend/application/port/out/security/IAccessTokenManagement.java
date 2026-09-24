package com.fidely.backend.application.port.out.security;

import java.util.UUID;

/**
 * Port de sortie permettant de gérer les access tokens
 * utilisés pour authentifier les utilisateurs.
 */
public interface IAccessTokenManagement {

    /**
     * Génère un access token pour un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @param role rôle applicatif de l'utilisateur
     * @return access token généré
     */
    String generate(UUID userId, String role);

    /**
     * Extrait l'identifiant utilisateur d'un access token valide.
     *
     * @param accessToken access token
     * @return identifiant de l'utilisateur
     */
    UUID extractUserId(String accessToken);

    /**
     * Extrait le rôle d'un access token valide.
     *
     * @param accessToken access token
     * @return rôle de l'utilisateur
     */
    String extractRole(String accessToken);
}