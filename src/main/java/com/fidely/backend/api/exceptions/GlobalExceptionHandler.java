package com.fidely.backend.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;

/**
 * Gestionnaire global des exceptions levées par l'API Fidely.
 *
 * <p>Ce composant centralise la transformation des exceptions
 * applicatives en réponses HTTP cohérentes.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs liées à une requête invalide.
     *
     * @param exception exception levée
     * @return réponse HTTP 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    /**
     * Gère les erreurs liées à un état métier incompatible
     * avec l'opération demandée.
     *
     * @param exception exception levée
     * @return réponse HTTP 409
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
            IllegalStateException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    /**
     * Gère l'absence d'un paramètre obligatoire dans une requête HTTP.
     *
     * @param exception exception levée
     * @return réponse HTTP 400
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Paramètre obligatoire manquant : "
                        + exception.getParameterName()
        );
    }

    /**
     * Gère le dépassement de la taille maximale autorisée
     * pour l'envoi d'un fichier.
     *
     * @param exception exception levée
     * @return réponse HTTP 413
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception
    ) {
        return buildResponse(
                HttpStatus.CONTENT_TOO_LARGE,
                "Le fichier envoyé est trop volumineux."
        );
    }

    /**
     * Gère les exceptions inattendues non prévues par
     * les gestionnaires spécifiques.
     *
     * @param exception exception levée
     * @return réponse HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception exception
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue."
        );
    }

    /**
     * Construit une réponse d'erreur standardisée.
     *
     * @param status statut HTTP
     * @param message message d'erreur
     * @return réponse HTTP standardisée
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Gère les erreurs de validation d'un corps de requête.
     *
     * <p>Cette exception est notamment levée lorsqu'un objet annoté
     * avec {@code @Valid} ne respecte pas les contraintes de validation
     * déclarées sur ses champs.</p>
     *
     * @param exception exception de validation levée par Spring
     * @return réponse HTTP 400 contenant le message d'erreur
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("La requête est invalide.");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    /**
     * Gère l'absence d'une partie obligatoire d'une requête multipart.
     *
     * <p>Cette exception est notamment levée lorsqu'un fichier attendu
     * par {@code @RequestParam} n'est pas fourni.</p>
     *
     * @param exception exception levée par Spring
     * @return réponse HTTP 400
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestPart(
            MissingServletRequestPartException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Partie obligatoire manquante : "
                        + exception.getRequestPartName()
        );
    }
}
