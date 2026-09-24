package com.fidely.backend.application.port.out.security;

/**
 * Port de sortie permettant de générer des refresh tokens
 * cryptographiquement aléatoires.
 *
 * <p>La couche application utilise ce port sans dépendre
 * de l'implémentation technique utilisée pour générer
 * les tokens.</p>
 *
 * <p>Le token généré est destiné à être remis au client.
 * Sa valeur en clair ne doit jamais être persistée en base
 * de données.</p>
 */
public interface IRefreshTokenGenerator {

    /**
     * Génère un nouveau refresh token aléatoire.
     *
     * @return refresh token en clair
     */
    String generate();
}