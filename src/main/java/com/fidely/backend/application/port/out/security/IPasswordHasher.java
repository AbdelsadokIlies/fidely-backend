package com.fidely.backend.application.port.out.security;

/**
 * Port de sortie permettant de gérer le hachage
 * et la vérification des mots de passe.
 *
 * <p>La couche application ne dépend pas de l'algorithme
 * ou de la bibliothèque utilisée pour sécuriser les mots
 * de passe.</p>
 */
public interface IPasswordHasher {

    /**
     * Hache un mot de passe en clair.
     *
     * @param rawPassword mot de passe en clair
     * @return mot de passe haché
     */
    String hash(String rawPassword);

    /**
     * Vérifie qu'un mot de passe en clair correspond
     * à un mot de passe haché.
     *
     * @param rawPassword mot de passe en clair
     * @param hashedPassword mot de passe haché
     * @return {@code true} si le mot de passe correspond,
     *         {@code false} sinon
     */
    boolean matches(
            String rawPassword,
            String hashedPassword
    );
}