package com.fidely.backend.application.port.out.security;

/**

 * Port sortant permettant de hacher et de vérifier
 * les tokens sensibles.
 */
public interface ITokenHasher {

    /**

     * Hache un token.
     *
     * @param token token en clair
     * @return hash du token
     */
    String hash(String token);

    /**

     * Vérifie qu'un token correspond à un hash.
     *
     * @param token token en clair
     * @param hash hash attendu
     * @return true si le token correspond au hash
     */
    boolean matches(String token, String hash);
}
