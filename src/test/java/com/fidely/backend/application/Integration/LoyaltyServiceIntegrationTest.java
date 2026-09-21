package com.fidely.backend.application.Integration;

import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import com.fidely.backend.repository.SpringDataLoyaltyRepository;
import com.fidely.backend.repository.SpringDataLoyaltyTransactionRepository;
import com.fidely.backend.repository.SpringDataPointRuleRepository;
import com.fidely.backend.repository.SpringDataTicketRepository;
import com.fidely.backend.repository.entities.loyalties.LoyaltyEntity;
import com.fidely.backend.repository.entities.loyalties.LoyaltyTransactionEntity;
import com.fidely.backend.repository.entities.loyalties.Rewards.PointRuleEntity;
import com.fidely.backend.repository.entities.tickets.TicketEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class LoyaltyServiceIntegrationTest {

    @Autowired
    private ILoyaltyService loyaltyService;

    @Autowired
    private SpringDataLoyaltyRepository loyaltyRepository;

    @Autowired
    private SpringDataLoyaltyTransactionRepository transactionRepository;

    @Autowired
    private SpringDataPointRuleRepository pointRuleRepository;

    @Autowired
    private SpringDataTicketRepository ticketRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldAddPointsCreateTransactionAndValidateTicket() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        createPointRule(
                pointRuleId,
                merchantId
        );

        createTicket(
                ticketId,
                merchantId,
                customerId
        );

        loyaltyService.addPointsFromTicket(ticketId);

        LoyaltyEntity loyalty =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        List<LoyaltyTransactionEntity> transactions =
                transactionRepository.findByTicketId(ticketId);

        assertThat(transactions)
                .hasSize(1);

        LoyaltyTransactionEntity transaction =
                transactions.get(0);

        assertThat(transaction.getLoyaltyId())
                .isEqualTo(loyaltyId);

        assertThat(transaction.getTicketId())
                .isEqualTo(ticketId);

        assertThat(transaction.getPoints())
                .isEqualTo(112);

        TicketEntity ticket =
                ticketRepository.findById(ticketId)
                        .orElseThrow();

        assertThat(ticket.getStatus())
                .isEqualTo(TicketStatus.VALIDATED);
    }

    @Test
    void shouldNotAwardPointsTwiceForSameTicket() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        createPointRule(
                pointRuleId,
                merchantId
        );

        createTicket(
                ticketId,
                merchantId,
                customerId
        );

        // Premier traitement
        loyaltyService.addPointsFromTicket(ticketId);

        // Vérification du premier traitement
        LoyaltyEntity loyaltyAfterFirstCall =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyaltyAfterFirstCall.getPointsBalance())
                .isEqualTo(112);

        assertThat(
                transactionRepository.findByTicketId(ticketId)
        )
                .hasSize(1);

        // Deuxième traitement
        assertThatThrownBy(() ->
                loyaltyService.addPointsFromTicket(ticketId)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Points already awarded for ticket : " + ticketId
                );

        // Le solde ne doit pas avoir changé
        LoyaltyEntity loyaltyAfterSecondCall =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyaltyAfterSecondCall.getPointsBalance())
                .isEqualTo(112);

        // Toujours une seule transaction
        assertThat(
                transactionRepository.findByTicketId(ticketId)
        )
                .hasSize(1);

        // Le ticket reste validé
        TicketEntity ticket =
                ticketRepository.findById(ticketId)
                        .orElseThrow();

        assertThat(ticket.getStatus())
                .isEqualTo(TicketStatus.VALIDATED);
    }

    @Test
    void shouldAwardPointsOnlyOnceWhenSameTicketIsProcessedConcurrently()
            throws Exception {

        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        createPointRule(
                pointRuleId,
                merchantId
        );

        createTicket(
                ticketId,
                merchantId,
                customerId
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Callable<Boolean> task = () -> {
            try {
                loyaltyService.addPointsFromTicket(ticketId);
                return true;
            } catch (Exception e) {
                return false;
            }
        };

        try {
            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);

            boolean firstSucceeded = first.get();
            boolean secondSucceeded = second.get();

            int successCount = 0;

            if (firstSucceeded) {
                successCount++;
            }

            if (secondSucceeded) {
                successCount++;
            }

            assertThat(successCount)
                    .isEqualTo(1);

        } finally {
            executor.shutdown();
        }

        LoyaltyEntity loyalty =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        assertThat(
                transactionRepository.findByTicketId(ticketId)
        )
                .hasSize(1);

        TicketEntity ticket =
                ticketRepository.findById(ticketId)
                        .orElseThrow();

        assertThat(ticket.getStatus())
                .isEqualTo(TicketStatus.VALIDATED);
    }

    private void createLoyalty(
            UUID id,
            UUID customerId,
            UUID merchantId
    ) {
        loyaltyRepository.saveAndFlush(
                new LoyaltyEntity(
                        id,
                        customerId,
                        merchantId,
                        0,
                        LocalDateTime.of(2026, 6, 15, 12, 0),
                        LocalDateTime.of(2026, 6, 15, 12, 0)
                )
        );
    }

    @Test
    void shouldNotLosePointsWhenDifferentTicketsAreProcessedConcurrently()
            throws Exception {

        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID ticketId1 = UUID.randomUUID();
        UUID ticketId2 = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        createLoyalty(
                loyaltyId,
                customerId,
                merchantId
        );

        createPointRule(
                pointRuleId,
                merchantId
        );

        createTicket(
                ticketId1,
                merchantId,
                customerId
        );

        createTicket(
                ticketId2,
                merchantId,
                customerId
        );

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Callable<Boolean> task1 = () -> {
            try {
                loyaltyService.addPointsFromTicket(ticketId1);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };

        Callable<Boolean> task2 = () -> {
            try {
                loyaltyService.addPointsFromTicket(ticketId2);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };

        try {
            Future<Boolean> first =
                    executor.submit(task1);

            Future<Boolean> second =
                    executor.submit(task2);

            boolean firstSucceeded =
                    first.get();

            boolean secondSucceeded =
                    second.get();

            assertThat(firstSucceeded)
                    .isTrue();

            assertThat(secondSucceeded)
                    .isTrue();

        } finally {
            executor.shutdown();
        }

        LoyaltyEntity loyalty =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(224);

        assertThat(
                transactionRepository.findByTicketId(ticketId1)
        )
                .hasSize(1);

        assertThat(
                transactionRepository.findByTicketId(ticketId2)
        )
                .hasSize(1);

        assertThat(
                transactionRepository.findByLoyaltyId(loyaltyId)
        )
                .hasSize(2);

        assertThat(
                ticketRepository.findById(ticketId1)
                        .orElseThrow()
                        .getStatus()
        )
                .isEqualTo(TicketStatus.VALIDATED);

        assertThat(
                ticketRepository.findById(ticketId2)
                        .orElseThrow()
                        .getStatus()
        )
                .isEqualTo(TicketStatus.VALIDATED);
    }

    private void createPointRule(
            UUID id,
            UUID merchantId
    ) {
        pointRuleRepository.saveAndFlush(
                new PointRuleEntity(
                        id,
                        merchantId,
                        new BigDecimal("1.0000"),
                        roundingMethodValue(RoundingMethod.FLOOR),
                        true,
                        LocalDateTime.of(2026, 1, 1, 0, 0),
                        LocalDateTime.of(2026, 12, 31, 23, 59),
                        LocalDateTime.of(2026, 1, 1, 0, 0)
                )
        );
    }

    private void createTicket(
            UUID id,
            UUID merchantId,
            UUID customerId
    ) {
        ticketRepository.saveAndFlush(
                new TicketEntity(
                        id,
                        merchantId,
                        customerId,
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
                )
        );
    }

    private short roundingMethodValue(
            RoundingMethod roundingMethod
    ) {
        return switch (roundingMethod) {
            case FLOOR -> 0;
            case ROUND -> 1;
            case CEIL -> 2;
        };
    }

    private void insertMerchant(UUID id) {
        jdbcTemplate.update(
                """
                INSERT INTO merchants (id, name, slug)
                VALUES (?, ?, ?)
                """,
                id,
                "Merchant " + id,
                "merchant-" + id
        );
    }

    private void insertCustomer(UUID id) {
        jdbcTemplate.update(
                """
                INSERT INTO users
                    (id, email, first_name, last_name, password_hash)
                VALUES (?, ?, ?, ?, ?)
                """,
                id,
                id + "@test.com",
                "Test",
                "User",
                "hash"
        );
    }
}