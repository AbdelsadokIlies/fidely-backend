package com.fidely.backend.api.dtos.models.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données nécessaires pour réinitialiser un mot de passe.
 *
 * @param token token de réinitialisation
 * @param newPassword nouveau mot de passe
 */
public record ResetPasswordRequest(

        @NotBlank(message = "Reset token is required")
        String token,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                message = "Password must contain at least 8 characters"
        )
        String newPassword

) {
}