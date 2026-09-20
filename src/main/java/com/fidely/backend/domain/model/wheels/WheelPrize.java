package com.fidely.backend.domain.model.wheels;

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
    private final Wheel wheel;
    private final String label;
    private final int probabilityWeight;
    private final LocalDateTime createdAt;

    /**

     * Crée un nouveau lot pour une roue de récompenses.
     *
     * @param id identifiant du lot
     * @param wheel roue à laquelle le lot est associé
     * @param label libellé du lot
     * @param probabilityWeight poids de probabilité du lot
     * @param createdAt date de création du lot
     */
    public WheelPrize(
            UUID id,
            Wheel wheel,
            String label,
            int probabilityWeight,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.wheel = wheel;
        this.label = label;
        this.probabilityWeight = probabilityWeight;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Wheel getWheel() {
        return wheel;
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
