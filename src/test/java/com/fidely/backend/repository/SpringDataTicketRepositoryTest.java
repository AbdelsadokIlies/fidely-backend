package com.fidely.backend.repository;

import com.fidely.backend.domain.models.tickets.TicketStatus;
import com.fidely.backend.repository.entities.tickets.TicketEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class SpringDataTicketRepositoryTest {

    @Autowired
    private SpringDataTicketRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndFindTicketById() {
        UUID ticketId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        TicketEntity ticket = createTicket(
                ticketId,
                merchantId,
                customerId,
                "TICKET-001"
        );

        repository.save(ticket);

        Optional<TicketEntity> result =
                repository.findById(ticketId);

        assertThat(result)
                .isPresent()
                .get()
                .extracting(TicketEntity::getTicketNumber)
                .isEqualTo("TICKET-001");
    }

    @Test
    void shouldFindTicketByTicketNumber() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        TicketEntity ticket = createTicket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-002"
        );

        repository.save(ticket);

        Optional<TicketEntity> result =
                repository.findByTicketNumber("TICKET-002");

        assertThat(result)
                .isPresent()
                .get()
                .extracting(TicketEntity::getId)
                .isEqualTo(ticket.getId());
    }

    @Test
    void shouldFindTicketsByCustomerId() {
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        repository.save(createTicket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-003"
        ));

        repository.save(createTicket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-004"
        ));

        List<TicketEntity> result =
                repository.findByCustomerId(customerId);

        assertThat(result)
                .hasSize(2)
                .extracting(TicketEntity::getCustomerId)
                .containsOnly(customerId);
    }

    @Test
    void shouldFindTicketsByMerchantId() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        repository.save(createTicket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-005"
        ));

        repository.save(createTicket(
                UUID.randomUUID(),
                merchantId,
                UUID.randomUUID(),
                "TICKET-006"
        ));

        List<TicketEntity> result =
                repository.findByMerchantId(merchantId);

        assertThat(result)
                .hasSize(2)
                .extracting(TicketEntity::getMerchantId)
                .containsOnly(merchantId);
    }

    @Test
    void shouldFindTicketsByCustomerAndMerchant() {
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        repository.save(createTicket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-007"
        ));

        repository.save(createTicket(
                UUID.randomUUID(),
                merchantId,
                UUID.randomUUID(),
                "TICKET-008"
        ));

        repository.save(createTicket(
                UUID.randomUUID(),
                UUID.randomUUID(),
                customerId,
                "TICKET-009"
        ));

        List<TicketEntity> result =
                repository.findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                );

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(TicketEntity::getTicketNumber)
                .isEqualTo("TICKET-007");
    }

    @Test
    void shouldFindTicketByFingerprintHash() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        TicketEntity ticket = createTicket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-010"
        );

        repository.save(ticket);

        Optional<TicketEntity> result =
                repository.findByFingerprintHash(
                        ticket.getFingerprintHash()
                );

        assertThat(result)
                .isPresent()
                .get()
                .extracting(TicketEntity::getId)
                .isEqualTo(ticket.getId());
    }

    @Test
    void shouldReturnEmptyWhenFingerprintHashDoesNotExist() {
        Optional<TicketEntity> result =
                repository.findByFingerprintHash(
                        "UNKNOWN-FINGERPRINT"
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenFingerprintHashExists() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        TicketEntity ticket = createTicket(
                UUID.randomUUID(),
                merchantId,
                customerId,
                "TICKET-011"
        );

        repository.save(ticket);

        boolean result =
                repository.existsByFingerprintHash(
                        ticket.getFingerprintHash()
                );

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFingerprintHashDoesNotExist() {
        boolean result =
                repository.existsByFingerprintHash(
                        "UNKNOWN-FINGERPRINT"
                );

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenTicketNumberDoesNotExist() {
        Optional<TicketEntity> result =
                repository.findByTicketNumber("UNKNOWN-TICKET");

        assertThat(result).isEmpty();
    }

    /**
     * Crée une entité Ticket pour les tests.
     *
     * <p>Insère au préalable les lignes marchand et utilisateur requises
     * par les contraintes de clé étrangère de la table "tickets".</p>
     *
     * @param id identifiant du ticket
     * @param merchantId identifiant du marchand
     * @param customerId identifiant du client
     * @param ticketNumber numéro du ticket
     * @return ticket de test
     */
    private TicketEntity createTicket(
            UUID id,
            UUID merchantId,
            UUID customerId,
            String ticketNumber
    ) {
        insertMerchant(merchantId);
        insertCustomer(customerId);

        return new TicketEntity(
                id,
                merchantId,
                customerId,
                ticketNumber,
                "fingerprint-" + ticketNumber,
                LocalDate.of(2026, 9, 20),
                LocalTime.of(14, 30),
                new BigDecimal("42.50"),
                "OCR TEXT",
                "image-" + ticketNumber,
                TicketStatus.PENDING,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Insère un marchand minimal en base pour satisfaire la contrainte
     * de clé étrangère "tickets_merchant_id_fkey".
     *
     * <p>"ON CONFLICT DO NOTHING" permet de réutiliser le même merchantId
     * dans plusieurs tickets d'un même test sans provoquer de doublon.</p>
     *
     * @param id identifiant du marchand à insérer
     */
    private void insertMerchant(UUID id) {
        jdbcTemplate.update(
                "INSERT INTO merchants (id, name, slug) VALUES (?, ?, ?) " +
                        "ON CONFLICT (id) DO NOTHING",
                id,
                "Merchant " + id,
                "merchant-" + id
        );
    }

    /**
     * Insère un utilisateur minimal en base pour satisfaire la contrainte
     * de clé étrangère "tickets_customer_id_fkey".
     *
     * @param id identifiant du client à insérer
     */
    private void insertCustomer(UUID id) {
        jdbcTemplate.update(
                "INSERT INTO users (id, email, first_name, last_name, password_hash) " +
                        "VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                id,
                id + "@test.com",
                "Test",
                "User",
                "hash"
        );
    }
}