package com.fidely.backend.domain.models.wheels;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente un lot pouvant être obtenu lors d'une participation à une roue.
 *
 * <p>Chaque lot possède un poids de probabilité utilisé pour déterminer
 * sa fréquence d'apparition lors du tirage.</p>
 */
public class WheelPrize {

    private final UUID id;
    private final UUID wheelId;
    private final String label;
    private final int probabilityWeight;
    private final LocalDateTime createdAt;

    /**

     * Crée un nouveau lot pour une roue de récompenses.
     *
     * @param id identifiant du lot
     * @param wheelId id de la roue à laquelle le lot est associé
     * @param label libellé du lot
     * @param probabilityWeight poids de probabilité du lot
     * @param createdAt date de création du lot
     */
    public WheelPrize(
            UUID id,
            UUID wheelId,
            String label,
            int probabilityWeight,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.wheelId = wheelId;
        this.label = label;
        this.probabilityWeight = probabilityWeight;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getWheelId() {
        return wheelId;
    }

    public String getLabel() {
        return label;
    }

    public int getProbabilityWeight() {
        return probabilityWeight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
