package com.fidely.backend.api.dtos.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Données nécessaires pour renvoyer un email de vérification.
 *
 * @param email adresse email de l'utilisateur
 */
public record ResendVerificationEmailRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email

) {
}