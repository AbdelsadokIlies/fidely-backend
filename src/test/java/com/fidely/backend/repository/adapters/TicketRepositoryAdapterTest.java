package com.fidely.backend.repository.adapters;

import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import com.fidely.backend.repository.SpringDataTicketRepository;
import com.fidely.backend.repository.entities.tickets.TicketEntity;
import com.fidely.backend.repository.mappers.tickets.TicketMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketRepositoryAdapterTest {

    @Mock
    private SpringDataTicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    private TicketRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TicketRepositoryAdapter(
                ticketRepository,
                ticketMapper
        );
    }

    @Test
    void shouldFindTicketById() {
        UUID id = UUID.randomUUID();

        TicketEntity entity = createEntity(id);
        Ticket domain = createDomain(id);

        when(ticketRepository.findById(id))
                .thenReturn(Optional.of(entity));

        when(ticketMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<Ticket> result = adapter.findById(id);

        assertThat(result)
                .isPresent()
                .contains(domain);

        verify(ticketRepository).findById(id);
        verify(ticketMapper).toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenTicketDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(ticketRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<Ticket> result = adapter.findById(id);

        assertThat(result).isEmpty();

        verify(ticketRepository).findById(id);
        verifyNoInteractions(ticketMapper);
    }

    @Test
    void shouldFindTicketByNumber() {
        String ticketNumber = "TICKET-123";

        TicketEntity entity = createEntity(UUID.randomUUID());
        Ticket domain = createDomain(entity.getId());

        when(ticketRepository.findByTicketNumber(ticketNumber))
                .thenReturn(Optional.of(entity));

        when(ticketMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<Ticket> result =
                adapter.findByTicketNumber(ticketNumber);

        assertThat(result)
                .isPresent()
                .contains(domain);

        verify(ticketRepository)
                .findByTicketNumber(ticketNumber);

        verify(ticketMapper)
                .toDomain(entity);
    }

    @Test
    void shouldFindTicketsByCustomerId() {
        UUID customerId = UUID.randomUUID();

        TicketEntity entity1 = createEntity(UUID.randomUUID());
        TicketEntity entity2 = createEntity(UUID.randomUUID());

        Ticket ticket1 = createDomain(entity1.getId());
        Ticket ticket2 = createDomain(entity2.getId());

        when(ticketRepository.findByCustomerId(customerId))
                .thenReturn(List.of(entity1, entity2));

        when(ticketMapper.toDomain(entity1))
                .thenReturn(ticket1);

        when(ticketMapper.toDomain(entity2))
                .thenReturn(ticket2);

        List<Ticket> result =
                adapter.findByCustomerId(customerId);

        assertThat(result)
                .containsExactly(ticket1, ticket2);

        verify(ticketRepository)
                .findByCustomerId(customerId);

        verify(ticketMapper)
                .toDomain(entity1);

        verify(ticketMapper)
                .toDomain(entity2);
    }

    @Test
    void shouldFindTicketsByMerchantId() {
        UUID merchantId = UUID.randomUUID();

        TicketEntity entity1 = createEntity(UUID.randomUUID());
        TicketEntity entity2 = createEntity(UUID.randomUUID());

        Ticket ticket1 = createDomain(entity1.getId());
        Ticket ticket2 = createDomain(entity2.getId());

        when(ticketRepository.findByMerchantId(merchantId))
                .thenReturn(List.of(entity1, entity2));

        when(ticketMapper.toDomain(entity1))
                .thenReturn(ticket1);

        when(ticketMapper.toDomain(entity2))
                .thenReturn(ticket2);

        List<Ticket> result =
                adapter.findByMerchantId(merchantId);

        assertThat(result)
                .containsExactly(ticket1, ticket2);

        verify(ticketRepository)
                .findByMerchantId(merchantId);

        verify(ticketMapper)
                .toDomain(entity1);

        verify(ticketMapper)
                .toDomain(entity2);
    }

    @Test
    void shouldFindTicketsByCustomerAndMerchant() {
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        TicketEntity entity1 = createEntity(UUID.randomUUID());
        TicketEntity entity2 = createEntity(UUID.randomUUID());

        Ticket ticket1 = createDomain(entity1.getId());
        Ticket ticket2 = createDomain(entity2.getId());

        when(ticketRepository.findByCustomerIdAndMerchantId(
                customerId, merchantId))
                .thenReturn(List.of(entity1, entity2));

        when(ticketMapper.toDomain(entity1))
                .thenReturn(ticket1);

        when(ticketMapper.toDomain(entity2))
                .thenReturn(ticket2);

        List<Ticket> result =
                adapter.findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                );

        assertThat(result)
                .containsExactly(ticket1, ticket2);

        verify(ticketRepository)
                .findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                );

        verify(ticketMapper).toDomain(entity1);
        verify(ticketMapper).toDomain(entity2);
    }

    @Test
    void shouldSaveTicket() {
        UUID id = UUID.randomUUID();

        Ticket domain = createDomain(id);
        TicketEntity entity = createEntity(id);
        TicketEntity savedEntity = createEntity(id);
        Ticket savedDomain = createDomain(id);

        when(ticketMapper.toEntity(domain))
                .thenReturn(entity);

        when(ticketRepository.save(entity))
                .thenReturn(savedEntity);

        when(ticketMapper.toDomain(savedEntity))
                .thenReturn(savedDomain);

        Ticket result = adapter.save(domain);

        assertThat(result)
                .isSameAs(savedDomain);

        verify(ticketMapper).toEntity(domain);
        verify(ticketRepository).save(entity);
        verify(ticketMapper).toDomain(savedEntity);
    }

    @Test
    void shouldDeleteTicketById() {
        UUID id = UUID.randomUUID();

        adapter.deleteById(id);

        verify(ticketRepository).deleteById(id);
    }

    @Test
    void shouldFindTicketByFingerprintHash() {
        String fingerprintHash = "fingerprint-123";

        TicketEntity entity = createEntity(UUID.randomUUID());
        Ticket domain = createDomain(entity.getId());

        when(ticketRepository.findByFingerprintHash(fingerprintHash))
                .thenReturn(Optional.of(entity));

        when(ticketMapper.toDomain(entity))
                .thenReturn(domain);

        Optional<Ticket> result =
                adapter.findByFingerprintHash(fingerprintHash);

        assertThat(result)
                .isPresent()
                .contains(domain);

        verify(ticketRepository)
                .findByFingerprintHash(fingerprintHash);

        verify(ticketMapper)
                .toDomain(entity);
    }

    @Test
    void shouldReturnEmptyWhenFingerprintDoesNotExist() {
        String fingerprintHash = "unknown-fingerprint";

        when(ticketRepository.findByFingerprintHash(fingerprintHash))
                .thenReturn(Optional.empty());

        Optional<Ticket> result =
                adapter.findByFingerprintHash(fingerprintHash);

        assertThat(result).isEmpty();

        verify(ticketRepository)
                .findByFingerprintHash(fingerprintHash);

        verifyNoInteractions(ticketMapper);
    }

    @Test
    void shouldCheckIfFingerprintExists() {
        String fingerprintHash = "fingerprint-123";

        when(ticketRepository.existsByFingerprintHash(fingerprintHash))
                .thenReturn(true);

        boolean result =
                adapter.existsByFingerprintHash(fingerprintHash);

        assertThat(result).isTrue();

        verify(ticketRepository)
                .existsByFingerprintHash(fingerprintHash);
    }

    @Test
    void shouldReturnFalseWhenFingerprintDoesNotExist() {
        String fingerprintHash = "unknown-fingerprint";

        when(ticketRepository.existsByFingerprintHash(fingerprintHash))
                .thenReturn(false);

        boolean result =
                adapter.existsByFingerprintHash(fingerprintHash);

        assertThat(result).isFalse();

        verify(ticketRepository)
                .existsByFingerprintHash(fingerprintHash);
    }

    private TicketEntity createEntity(UUID id) {
        return new TicketEntity(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TICKET-" + id,
                "fingerprint-" + id,
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                "storage/" + id,
                TicketStatus.PENDING,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );
    }

    private Ticket createDomain(UUID id) {
        return new Ticket(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TICKET-" + id,
                "fingerprint-" + id,
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 30),
                new BigDecimal("112.00"),
                "OCR TEXT",
                "storage/" + id,
                TicketStatus.PENDING,
                null,
                LocalDateTime.of(2026, 6, 15, 12, 30)
        );
    }
}