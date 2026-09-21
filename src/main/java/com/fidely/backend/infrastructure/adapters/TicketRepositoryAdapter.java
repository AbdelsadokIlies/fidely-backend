package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.application.port.out.ITicketRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataTicketRepository;
import com.fidely.backend.infrastructure.entities.tickets.TicketEntity;
import com.fidely.backend.infrastructure.mappers.tickets.TicketMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter permettant au domaine d'accéder à la persistance
 * des tickets via Spring Data JPA.
 */
@Repository
public class TicketRepositoryAdapter implements ITicketRepository {

    private final SpringDataTicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketRepositoryAdapter(
            SpringDataTicketRepository ticketRepository,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return ticketRepository.findById(id)
                .map(ticketMapper::toDomain);
    }

    @Override
    public Optional<Ticket> findByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .map(ticketMapper::toDomain);
    }

    @Override
    public List<Ticket> findByCustomerId(UUID customerId) {
        return ticketRepository.findByCustomerId(customerId)
                .stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public List<Ticket> findByMerchantId(UUID merchantId) {
        return ticketRepository.findByMerchantId(merchantId)
                .stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public List<Ticket> findByCustomerIdAndMerchantId(
            UUID customerId,
            UUID merchantId
    ) {
        return ticketRepository
                .findByCustomerIdAndMerchantId(customerId, merchantId)
                .stream()
                .map(ticketMapper::toDomain)
                .toList();
    }

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = ticketMapper.toEntity(ticket);

        TicketEntity savedEntity = ticketRepository.save(entity);

        return ticketMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        ticketRepository.deleteById(id);
    }

    @Override
    public Optional<Ticket> findByFingerprintHash(String fingerprintHash) {
        return ticketRepository.findByFingerprintHash(fingerprintHash)
                .map(ticketMapper::toDomain);
    }

    @Override
    public boolean existsByFingerprintHash(String fingerprintHash) {
        return ticketRepository.existsByFingerprintHash(fingerprintHash);
    }
}