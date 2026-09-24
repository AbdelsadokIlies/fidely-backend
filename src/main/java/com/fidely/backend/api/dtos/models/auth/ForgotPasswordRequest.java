package com.fidely.backend.api.dtos.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Données nécessaires pour demander une réinitialisation
 * de mot de passe.
 *
 * @param email adresse email du compte
 */
public record ForgotPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email

) {
}