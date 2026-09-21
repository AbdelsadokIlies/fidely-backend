package com.fidely.backend.domain.models.tickets;

/**

 * Représente l'état de traitement d'un ticket.
 */
public enum TicketStatus {

    PENDING,

    VALIDATED,

    REJECTED,

    DUPLICATE
}
