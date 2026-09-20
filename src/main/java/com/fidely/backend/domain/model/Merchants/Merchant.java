package com.fidely.backend.domain.model.Merchants;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente un marchand utilisant la plateforme Fidely.
 *
 * <p>Un marchand possède notamment ses informations d'identification,
 * sa personnalisation visuelle et son statut d'activité.</p>
 */
public class Merchant {

    private final UUID id;
    private final String name;
    private final String slug;
    private final String logoUrl;
    private final String primaryColor;
    private final String secondaryColor;
    private final String description;
    private final String googleReviewUrl;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**

     * Crée un nouveau marchand.
     *
     * @param id identifiant du marchand
     * @param name nom du marchand
     * @param slug identifiant textuel unique du marchand
     * @param logoUrl URL du logo du marchand
     * @param primaryColor couleur principale utilisée pour la personnalisation
     * @param secondaryColor couleur secondaire utilisée pour la personnalisation
     * @param description description du marchand
     * @param googleReviewUrl URL permettant de laisser un avis Google
     * @param active indique si le marchand est actif
     * @param createdAt date de création du marchand
     * @param updatedAt date de dernière modification du marchand
     */
    public Merchant(
            UUID id,
            String name,
            String slug,
            String logoUrl,
            String primaryColor,
            String secondaryColor,
            String description,
            String googleReviewUrl,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.logoUrl = logoUrl;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.description = description;
        this.googleReviewUrl = googleReviewUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public String getDescription() {
        return description;
    }

    public String getGoogleReviewUrl() {
        return googleReviewUrl;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
