package com.fidely.backend.domain.models.notifications;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente une notification envoyée à un utilisateur.
 *
 * <p>Une notification contient un type, un titre, un message et des
 * informations permettant de suivre son envoi et sa lecture.</p>
 */
public class Notification {

    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final boolean read;
    private final LocalDateTime sentAt;
    private final LocalDateTime createdAt;

    /**

     * Crée une nouvelle notification.
     *
     * @param id identifiant de la notification
     * @param userId id de l'utilisateur destinataire de la notification
     * @param type type de notification
     * @param title titre de la notification
     * @param message contenu de la notification
     * @param read indique si la notification a été lue
     * @param sentAt date d'envoi de la notification
     * @param createdAt date de création de la notification
     */
    public Notification(
            UUID id,
            UUID userId,
            NotificationType type,
            String title,
            String message,
            boolean read,
            LocalDateTime sentAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.read = read;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
