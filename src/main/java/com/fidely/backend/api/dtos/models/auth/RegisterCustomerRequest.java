package com.fidely.backend.api.dtos.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Requête permettant de créer un compte client.
 *
 * <p>Ce DTO représente les données reçues par l'API lors
 * de l'inscription d'un client. Les règles métier restent
 * gérées par la couche application.</p>
 *
 * @param email adresse e-mail du client
 * @param firstName prénom du client
 * @param lastName nom du client
 * @param phone numéro de téléphone du client
 * @param birthDate date de naissance du client
 * @param password mot de passe du compte
 */
public record RegisterCustomerRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String phone,

        LocalDate birthDate,

        @NotBlank(message = "Password is required")
        String password
) {
}