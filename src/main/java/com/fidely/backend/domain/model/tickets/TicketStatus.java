package com.fidely.backend.domain.model.tickets;

/**

 * Représente l'état de traitement d'un ticket.
 */
public enum TicketStatus {

    PENDING,

    VALIDATED,

    REJECTED,

    DUPLICATE
}
