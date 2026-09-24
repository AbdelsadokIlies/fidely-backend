package com.fidely.backend.infrastructure.email;

import com.fidely.backend.application.port.out.IEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implémentation simulée de l'envoi d'emails.
 *
 * <p>Utilisée temporairement en développement.
 * Aucun email réel n'est envoyé.</p>
 */
@Component
public class SimulatedEmailSender implements IEmailSender {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SimulatedEmailSender.class);

    @Override
    public void send(
            String recipient,
            String subject,
            String body
    ) {
        LOGGER.info(
                """
                ===== EMAIL SIMULÉ =====
                Destinataire : {}
                Sujet        : {}
                Contenu      :
                {}
                ========================
                """,
                recipient,
                subject,
                body
        );
    }
}