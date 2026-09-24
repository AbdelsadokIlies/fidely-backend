package com.fidely.backend.api.dtos.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requête permettant de créer un compte gestionnaire de marchand.
 *
 * <p>Ce DTO représente les données reçues par l'API lors
 * de l'inscription d'un gestionnaire de marchand. Les règles
 * métier restent gérées par la couche application.</p>
 *
 * @param merchantId identifiant du marchand associé au compte
 * @param email adresse e-mail du gestionnaire
 * @param firstName prénom du gestionnaire
 * @param lastName nom du gestionnaire
 * @param password mot de passe du compte
 */
public record RegisterMerchantRequest(

        @NotNull(message = "Merchant ID is required")
        UUID merchantId,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Password is required")
        String password
) {
}