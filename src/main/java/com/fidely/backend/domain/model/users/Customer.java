package com.fidely.backend.domain.model.users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente un client de la plateforme Fidely.
 *
 * <p>Un client est associé à un compte utilisateur et possède ses
 * informations personnelles nécessaires à son identification.</p>
 */
public class Customer {

    private final UUID id;
    private final User user;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final LocalDate birthDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**

     * Crée un nouveau client.
     *
     * @param id identifiant du client
     * @param user compte utilisateur associé au client
     * @param firstName prénom du client
     * @param lastName nom du client
     * @param phone numéro de téléphone du client
     * @param birthDate date de naissance du client
     * @param createdAt date de création du client
     * @param updatedAt date de dernière modification du client
     */
    public Customer(
            UUID id,
            User user,
            String firstName,
            String lastName,
            String phone,
            LocalDate birthDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
