package com.fidely.backend.application.port.out;

/**

 * Port sortant générique pour l'envoi d'emails.
 */
public interface IEmailSender {

    /**

     * Envoie un email.
     *
     * @param recipient adresse du destinataire
     * @param subject sujet de l'email
     * @param body contenu de l'email
     */
    void send(
            String recipient,
            String subject,
            String body
    );
}