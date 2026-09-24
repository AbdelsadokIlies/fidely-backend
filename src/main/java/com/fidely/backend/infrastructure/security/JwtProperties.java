package com.fidely.backend.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**

 * Propriétés de configuration utilisées pour la génération
 * et la validation des JWT d'authentification et de vérification email.
 *
 * <p>Les valeurs sont chargées depuis les propriétés Spring
 * préfixées par {@code fidely.security.jwt}.</p>
 *
 * <p>La clé secrète ne doit jamais être codée en dur dans le
 * code source. Elle doit être fournie par la configuration de
 * l'environnement, par exemple via une variable d'environnement.</p>
 */
@ConfigurationProperties(prefix = "fidely.security.jwt")
public class JwtProperties {

    private String secret;

    private long accessTokenExpirationMinutes = 15;

    private long emailVerificationTokenExpirationMinutes = 60;

    /**

     * Retourne la clé secrète utilisée pour signer les JWT.
     *
     * @return clé secrète
     */
    public String getSecret() {
        return secret;
    }

    /**

     * Définit la clé secrète utilisée pour signer les JWT.
     *
     * @param secret clé secrète
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**

     * Retourne la durée de validité d'un access token en minutes.
     *
     * @return durée de validité de l'access token
     */
    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    /**

     * Définit la durée de validité d'un access token en minutes.
     *
     * @param accessTokenExpirationMinutes durée de validité
     */
    public void setAccessTokenExpirationMinutes(
            long accessTokenExpirationMinutes
    ) {
        this.accessTokenExpirationMinutes =
                accessTokenExpirationMinutes;
    }

    /**

     * Retourne la durée de validité d'un token de vérification
     * d'adresse email en minutes.
     *
     * @return durée de validité du token de vérification email
     */
    public long getEmailVerificationTokenExpirationMinutes() {
        return emailVerificationTokenExpirationMinutes;
    }

    /**

     * Définit la durée de validité d'un token de vérification
     * d'adresse email en minutes.
     *
     * @param emailVerificationTokenExpirationMinutes durée de validité
     */
    public void setEmailVerificationTokenExpirationMinutes(
            long emailVerificationTokenExpirationMinutes
    ) {
        this.emailVerificationTokenExpirationMinutes =
                emailVerificationTokenExpirationMinutes;
    }
}
