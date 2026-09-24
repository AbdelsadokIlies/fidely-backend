package com.fidely.backend.api.dtos.models.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Requête permettant à un utilisateur de s'authentifier.
 *
 * <p>Les identifiants sont transmis à la couche application,
 * qui se charge de vérifier le compte et le mot de passe.</p>
 *
 * @param email adresse e-mail du compte
 * @param password mot de passe du compte
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}