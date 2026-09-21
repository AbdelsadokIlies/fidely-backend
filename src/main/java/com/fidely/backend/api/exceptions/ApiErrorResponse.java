package com.fidely.backend.api.exceptions;

import java.time.LocalDateTime;

/**
 * Représente la structure standard d'une erreur retournée
 * par l'API Fidely.
 *
 * @param status code HTTP de l'erreur
 * @param error nom de l'erreur HTTP
 * @param message message explicatif
 * @param timestamp date et heure de l'erreur
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
}

