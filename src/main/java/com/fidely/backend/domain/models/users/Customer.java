package com.fidely.backend.domain.models.users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Représente un client de la plateforme Fidely.
 *
 * <p>Un client est associé à un compte utilisateur et possède ses
 * informations personnelles nécessaires à son identification.</p>
 */
public class Customer extends User {

    private final String phone;
    private final LocalDate birthDate;

    /**

     * Crée un nouveau client.
     *
     * @param id identifiant du client
     * @param email email du client
     * @param firstName prénom du client
     * @param lastName nom du client
     * @param phone numéro de téléphone du client
     * @param passwordHash mot de passe haché du client
     * @param emailVerified indique si l'email à été vérifié
     * @param active indique si l'utilisateur est actif
     * @param birthDate date de naissance du client
     * @param createdAt date de création du client
     * @param updatedAt date de dernière modification du client
     */
    public Customer(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String phone,
            String passwordHash,
            boolean emailVerified,
            boolean active,
            LocalDate birthDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super(id,
                email,
                passwordHash,
                firstName,
                lastName,
                emailVerified,
                active,
                createdAt,
                updatedAt);
        this.phone = phone;
        this.birthDate = birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
}
