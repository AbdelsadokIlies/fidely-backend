package com.fidely.backend.infrastructure;

import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyTransactionRepository;
import com.fidely.backend.infrastructure.entities.models.loyalties.LoyaltyTransactionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class SpringDataLoyaltyTransactionRepositoryTest {

    @Autowired
    private SpringDataLoyaltyTransactionRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndFindTransactionById() {
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        insertLoyalty(loyaltyId);
        insertTicket(ticketId);

        LoyaltyTransactionEntity transaction =
                createTransaction(
                        transactionId,
                        loyaltyId,
                        ticketId,
                        100
                );

        repository.saveAndFlush(transaction);

        Optional<LoyaltyTransactionEntity> result =
                repository.findById(transactionId);

        assertThat(result)
                .isPresent()
                .get()
                .extracting(LoyaltyTransactionEntity::getPoints)
                .isEqualTo(100);
    }

    @Test
    void shouldFindTransactionsByLoyaltyId() {
        UUID loyaltyId = UUID.randomUUID();

        UUID ticketId1 = UUID.randomUUID();
        UUID ticketId2 = UUID.randomUUID();

        insertLoyalty(loyaltyId);
        insertTicket(ticketId1);
        insertTicket(ticketId2);

        repository.saveAndFlush(
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        ticketId1,
                        100
                )
        );

        repository.saveAndFlush(
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        ticketId2,
                        200
                )
        );

        List<LoyaltyTransactionEntity> result =
                repository.findByLoyaltyId(loyaltyId);

        assertThat(result)
                .hasSize(2)
                .extracting(LoyaltyTransactionEntity::getLoyaltyId)
                .containsOnly(loyaltyId);
    }

    @Test
    void shouldReturnEmptyWhenLoyaltyHasNoTransactions() {
        List<LoyaltyTransactionEntity> result =
                repository.findByLoyaltyId(UUID.randomUUID());

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldFindTransactionsByTicketId() {
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        insertLoyalty(loyaltyId);
        insertTicket(ticketId);

        repository.saveAndFlush(
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        ticketId,
                        100
                )
        );

        List<LoyaltyTransactionEntity> result =
                repository.findByTicketId(ticketId);

        assertThat(result)
                .hasSize(1)
                .extracting(LoyaltyTransactionEntity::getTicketId)
                .containsOnly(ticketId);
    }

    @Test
    void shouldReturnEmptyWhenTicketHasNoTransactions() {
        List<LoyaltyTransactionEntity> result =
                repository.findByTicketId(UUID.randomUUID());

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldAllowNullTicketId() {
        UUID loyaltyId = UUID.randomUUID();

        insertLoyalty(loyaltyId);

        LoyaltyTransactionEntity transaction =
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        null,
                        50
                );

        repository.saveAndFlush(transaction);

        Optional<LoyaltyTransactionEntity> result =
                repository.findById(transaction.getId());

        assertThat(result)
                .isPresent()
                .get()
                .extracting(LoyaltyTransactionEntity::getTicketId)
                .isNull();
    }

    @Test
    void shouldRejectSecondTransactionForSameTicket() {
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        insertLoyalty(loyaltyId);
        insertTicket(ticketId);

        LoyaltyTransactionEntity firstTransaction =
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        ticketId,
                        100
                );

        LoyaltyTransactionEntity secondTransaction =
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        ticketId,
                        50
                );

        repository.saveAndFlush(firstTransaction);

        assertThatThrownBy(() ->
                repository.saveAndFlush(secondTransaction)
        )
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowMultipleTransactionsWithoutTicket() {
        UUID loyaltyId = UUID.randomUUID();

        insertLoyalty(loyaltyId);

        LoyaltyTransactionEntity firstTransaction =
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        null,
                        100
                );

        LoyaltyTransactionEntity secondTransaction =
                createTransaction(
                        UUID.randomUUID(),
                        loyaltyId,
                        null,
                        50
                );

        repository.saveAndFlush(firstTransaction);
        repository.saveAndFlush(secondTransaction);

        assertThat(repository.findByLoyaltyId(loyaltyId))
                .hasSize(2)
                .extracting(LoyaltyTransactionEntity::getTicketId)
                .containsOnlyNulls();
    }

    private LoyaltyTransactionEntity createTransaction(
            UUID id,
            UUID loyaltyId,
            UUID ticketId,
            int points
    ) {
        return new LoyaltyTransactionEntity(
                id,
                loyaltyId,
                ticketId,
                points,
                "Test transaction",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    private void insertLoyalty(UUID loyaltyId) {
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        jdbcTemplate.update(
                "INSERT INTO loyalties " +
                        "(id, customer_id, merchant_id, points_balance, " +
                        "created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                loyaltyId,
                customerId,
                merchantId,
                100,
                LocalDateTime.of(2026, 1, 1, 9, 0),
                LocalDateTime.of(2026, 1, 1, 9, 0)
        );
    }

    private void insertTicket(UUID ticketId) {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        jdbcTemplate.update(
                "INSERT INTO tickets " +
                        "(id, merchant_id, customer_id, ticket_number, " +
                        "fingerprint_hash, ticket_date, ticket_time, amount, " +
                        "raw_ocr_text, status, " +
                        "rejection_reason, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                ticketId,
                merchantId,
                customerId,
                "TICKET-" + ticketId,
                "fingerprint-" + ticketId,
                LocalDate.of(2026, 1, 1),
                LocalTime.of(10, 0),
                BigDecimal.valueOf(100),
                "Test OCR",
                0,
                null,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    private void insertMerchant(UUID id) {
        jdbcTemplate.update(
                "INSERT INTO merchants (id, name, slug) " +
                        "VALUES (?, ?, ?) " +
                        "ON CONFLICT (id) DO NOTHING",
                id,
                "Merchant " + id,
                "merchant-" + id
        );
    }

    private void insertCustomer(UUID id) {
        jdbcTemplate.update(
                "INSERT INTO users " +
                        "(id, email, first_name, last_name) " +
                        "VALUES (?, ?, ?, ?)",
                id,
                id + "@test.com",
                "Test",
                "User"
        );
    }
}
