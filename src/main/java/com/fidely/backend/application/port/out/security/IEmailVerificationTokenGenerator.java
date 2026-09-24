package com.fidely.backend.application.port.out.security;

import java.util.UUID;

/**

 * Génère et valide les tokens utilisés pour la vérification
 * des adresses email.
 */
public interface IEmailVerificationTokenGenerator {

    /**

     * Génère un token de vérification pour un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return token de vérification signé
     */
    String generate(UUID userId);

    /**

     * Extrait l'identifiant utilisateur d'un token de vérification valide.
     *
     * @param token token de vérification
     * @return identifiant de l'utilisateur
     */
    UUID extractUserId(String token);
}
