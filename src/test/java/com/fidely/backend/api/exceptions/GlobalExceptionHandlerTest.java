package com.fidely.backend.api.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests du gestionnaire global des exceptions de l'API Fidely.
 *
 * <p>Ces tests vérifient que les différentes exceptions sont correctement
 * transformées en réponses HTTP standardisées.</p>
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    /**
     * Vérifie qu'une {@link IllegalArgumentException} produit
     * une réponse HTTP 400.
     */
    @Test
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Requête invalide");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleIllegalArgumentException(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().error())
                .isEqualTo("Bad Request");

        assertThat(response.getBody().message())
                .isEqualTo("Requête invalide");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    /**
     * Vérifie qu'une {@link IllegalStateException} produit
     * une réponse HTTP 409.
     */
    @Test
    void shouldHandleIllegalStateException() {
        IllegalStateException exception =
                new IllegalStateException("Ticket déjà validé");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleIllegalStateException(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(409);

        assertThat(response.getBody().error())
                .isEqualTo("Conflict");

        assertThat(response.getBody().message())
                .isEqualTo("Ticket déjà validé");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    /**
     * Vérifie qu'une absence de paramètre obligatoire produit
     * une réponse HTTP 400.
     */
    @Test
    void shouldHandleMissingServletRequestParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException(
                        "image",
                        "MultipartFile"
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleMissingServletRequestParameter(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().error())
                .isEqualTo("Bad Request");

        assertThat(response.getBody().message())
                .isEqualTo("Paramètre obligatoire manquant : image");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }

    /**
     * Vérifie qu'une exception inattendue produit
     * une réponse HTTP 500 sans exposer son détail interne.
     */
    @Test
    void shouldHandleUnexpectedException() {
        Exception exception =
                new RuntimeException("Erreur interne");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleException(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(500);

        assertThat(response.getBody().error())
                .isEqualTo("Internal Server Error");

        assertThat(response.getBody().message())
                .isEqualTo("Une erreur interne est survenue.");

        assertThat(response.getBody().timestamp())
                .isNotNull();
    }
}
