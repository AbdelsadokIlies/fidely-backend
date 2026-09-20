package com.fidely.backend.domain.model.loyalty;

import com.fidely.backend.domain.model.Merchants.Merchant;
import com.fidely.backend.domain.model.users.Customer;

import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente la relation de fidélité entre un client et un marchand.
 *
 * <p>Cette relation contient notamment le solde de points de fidélité
 * du client auprès du marchand.</p>
 */
public class Loyalty {

    private final UUID id;
    private final Customer customer;
    private final Merchant merchant;
    private int pointsBalance;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**

     * Crée une nouvelle relation de fidélité.
     *
     * @param id identifiant de la relation de fidélité
     * @param customer client associé à la fidélité
     * @param merchant marchand associé à la fidélité
     * @param pointsBalance solde initial de points
     * @param createdAt date de création
     * @param updatedAt date de dernière modification
     * @throws IllegalArgumentException si le solde est négatif,
     * si le client ou le marchand est null, si une date est null,
     * ou si la date de modification est antérieure à la date de création
     */
    public Loyalty(
            UUID id,
            Customer customer,
            Merchant merchant,
            int pointsBalance,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        if (pointsBalance < 0) {
            throw new IllegalArgumentException(
                    "Points balance cannot be negative"
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        if (merchant == null) {
            throw new IllegalArgumentException(
                    "Merchant cannot be null"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Created at cannot be null"
            );
        }

        if (updatedAt == null) {
            throw new IllegalArgumentException(
                    "Updated at cannot be null"
            );
        }

        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Updated at cannot be before created at"
            );
        }

        this.id = id;
        this.customer = customer;
        this.merchant = merchant;
        this.pointsBalance = pointsBalance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**

     * Ajoute des points au solde de fidélité.
     *
     * <p>La date de dernière modification est automatiquement mise à jour.</p>
     *
     * @param points nombre de points à ajouter
     * @throws IllegalArgumentException si le nombre de points est inférieur
     * ou égal à zéro
     */
    public void addPoints(int points) {

        if (points <= 0) {
            throw new IllegalArgumentException(
                    "Points must be positive"
            );
        }

        pointsBalance += points;
        updatedAt = LocalDateTime.now();
    }

    /**

     * Retire des points du solde de fidélité.
     *
     * <p>La date de dernière modification est automatiquement mise à jour.</p>
     *
     * @param points nombre de points à retirer
     * @throws IllegalArgumentException si le nombre de points est inférieur
     * ou égal à zéro
     * @throws IllegalStateException si le solde de points est insuffisant
     */
    public void removePoints(int points) {

        if (points <= 0) {
            throw new IllegalArgumentException(
                    "Points must be positive"
            );
        }

        if (points > pointsBalance) {
            throw new IllegalStateException(
                    "Insufficient points"
            );
        }

        pointsBalance -= points;
        updatedAt = LocalDateTime.now();
    }
}
