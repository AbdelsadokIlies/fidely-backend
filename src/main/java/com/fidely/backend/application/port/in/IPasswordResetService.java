package com.fidely.backend.application.port.in;

/**

 * Port entrant pour la réinitialisation des mots de passe.
 */
public interface IPasswordResetService {

    /**

     * Demande une réinitialisation de mot de passe.
     *
     * @param email adresse email de l'utilisateur
     * @return token de réinitialisation généré
     */
    String requestPasswordReset(String email);

    /**

     * Réinitialise le mot de passe à partir d'un token valide.
     *
     * @param token token de réinitialisation
     * @param newPassword nouveau mot de passe
     */
    void resetPassword(
            String token,
            String newPassword
    );
}