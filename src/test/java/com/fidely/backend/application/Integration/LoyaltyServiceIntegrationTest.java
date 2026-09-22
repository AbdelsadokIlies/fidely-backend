package com.fidely.backend.application.Integration;

import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.in.ITicketService;
import com.fidely.backend.application.port.out.IOcrService;
import com.fidely.backend.domain.models.loyalties.Rewards.RoundingMethod;
import com.fidely.backend.domain.models.tickets.Ticket;
import com.fidely.backend.domain.models.tickets.TicketStatus;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyTransactionRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataPointRuleRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataTicketRepository;
import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyEntity;
import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyTransactionEntity;
import com.fidely.backend.infrastructure.entities.loyalties.Rewards.PointRuleEntity;
import com.fidely.backend.infrastructure.entities.tickets.TicketEntity;
import com.fidely.backend.infrastructure.ocr.OcrTicketData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
import static org.mockito.Mockito.when;

/**
 * Tests d'intégration du workflow complet d'attribution
 * de points à partir d'un ticket de caisse.
 *
 * <p>Ces tests vérifient l'intégration entre le service de
 * fidélité, le service de ticket, le service OCR, les règles
 * de points, les transactions de fidélité et la persistance.</p>
 *
 * <p>Le service OCR est mocké afin que les tests contrôlent
 * précisément les données extraites des tickets sans dépendre
 * du comportement du {@code FakeOcrService}.</p>
 *
 * <p>Le workflow testé est le suivant :</p>
 *
 * <ol>
 *     <li>Une image est envoyée au service OCR.</li>
 *     <li>Les données OCR sont transformées en {@link Ticket}.</li>
 *     <li>Le ticket reste temporaire et n'est pas encore persisté.</li>
 *     <li>Le ticket est transmis au service de fidélité.</li>
 *     <li>Les points sont calculés et attribués.</li>
 *     <li>Le ticket et la transaction sont persistés.</li>
 *     <li>Le ticket est finalement validé.</li>
 * </ol>
 */
@SpringBootTest
class LoyaltyServiceIntegrationTest {

    @Autowired
    private ILoyaltyService loyaltyService;

    @Autowired
    private ITicketService ticketService;

    @MockitoBean
    private IOcrService ocrService;

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

    /**
     * Vérifie qu'une image de ticket permet de créer un ticket,
     * d'ajouter les points correspondants, de créer une
     * transaction et de valider le ticket.
     */
    @Test
    void shouldAddPointsCreateTransactionAndValidateTicket() {

        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        byte[] image = "fake-image".getBytes();

        BigDecimal ticketAmount =
                new BigDecimal("112.00");

        int expectedPoints = 112;

        when(ocrService.extractTicketData(image))
                .thenReturn(
                        createOcrTicketData(
                                "TICKET-1",
                                ticketAmount
                        )
                );

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

        LoyaltyEntity loyaltyBefore =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyaltyBefore.getPointsBalance())
                .isZero();

        Ticket ticket = ticketService.extractTicketFromOcr(
                image,
                merchantId,
                customerId
        );

        assertThat(ticket.getStatus())
                .isEqualTo(TicketStatus.PENDING);

        assertThat(ticketRepository.findById(ticket.getId()))
                .isEmpty();

        loyaltyService.addPointsFromTicket(
                loyaltyId,
                ticket
        );

        LoyaltyEntity loyalty =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(expectedPoints);

        List<LoyaltyTransactionEntity> transactions =
                transactionRepository.findByLoyaltyId(loyaltyId);

        assertThat(transactions)
                .hasSize(1);

        LoyaltyTransactionEntity transaction =
                transactions.get(0);

        assertThat(transaction.getLoyaltyId())
                .isEqualTo(loyaltyId);

        assertThat(transaction.getPoints())
                .isEqualTo(expectedPoints);

        assertThat(transaction.getTicketId())
                .isNotNull();

        TicketEntity ticketEntity =
                ticketRepository.findById(
                        transaction.getTicketId()
                ).orElseThrow();

        assertThat(ticketEntity.getMerchantId())
                .isEqualTo(merchantId);

        assertThat(ticketEntity.getCustomerId())
                .isEqualTo(customerId);

        assertThat(ticketEntity.getAmount())
                .isEqualByComparingTo(ticketAmount);

        assertThat(ticketEntity.getStatus())
                .isEqualTo(TicketStatus.VALIDATED);
    }

    /**
     * Vérifie que deux tickets différents traités
     * simultanément ne provoquent pas de perte de points.
     *
     * <p>Les données OCR sont contrôlées directement par le test.
     * Chaque ticket vaut 112 euros et génère donc 112 points
     * avec la règle utilisée dans ce scénario.</p>
     *
     * @throws Exception si l'exécution concurrente échoue
     */
    @Test
    void shouldNotLosePointsWhenDifferentTicketsAreProcessedConcurrently()
            throws Exception {

        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID loyaltyId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        byte[] image1 =
                "fake-image-1".getBytes();

        byte[] image2 =
                "fake-image-2".getBytes();

        BigDecimal ticketAmount =
                new BigDecimal("112.00");

        int expectedPointsPerTicket = 112;
        int expectedTotalPoints =
                expectedPointsPerTicket * 2;

        when(ocrService.extractTicketData(image1))
                .thenReturn(
                        createOcrTicketData(
                                "TICKET-1",
                                ticketAmount
                        )
                );

        when(ocrService.extractTicketData(image2))
                .thenReturn(
                        createOcrTicketData(
                                "TICKET-2",
                                ticketAmount
                        )
                );

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

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Callable<Boolean> task1 = () -> {
            try {
                Ticket ticket =
                        ticketService.extractTicketFromOcr(
                                image1,
                                merchantId,
                                customerId
                        );

                loyaltyService.addPointsFromTicket(
                        loyaltyId,
                        ticket
                );

                return true;

            } catch (Exception exception) {
                exception.printStackTrace();
                return false;
            }
        };

        Callable<Boolean> task2 = () -> {
            try {
                Ticket ticket =
                        ticketService.extractTicketFromOcr(
                                image2,
                                merchantId,
                                customerId
                        );

                loyaltyService.addPointsFromTicket(
                        loyaltyId,
                        ticket
                );

                return true;

            } catch (Exception exception) {
                exception.printStackTrace();
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
                .isEqualTo(expectedTotalPoints);

        List<LoyaltyTransactionEntity> transactions =
                transactionRepository.findByLoyaltyId(loyaltyId);

        assertThat(transactions)
                .hasSize(2);

        assertThat(
                transactions.stream()
                        .map(LoyaltyTransactionEntity::getPoints)
                        .toList()
        )
                .containsExactlyInAnyOrder(
                        expectedPointsPerTicket,
                        expectedPointsPerTicket
                );

        for (LoyaltyTransactionEntity transaction : transactions) {

            assertThat(transaction.getTicketId())
                    .isNotNull();

            TicketEntity ticket =
                    ticketRepository.findById(
                                    transaction.getTicketId()
                            )
                            .orElseThrow();

            assertThat(ticket.getStatus())
                    .isEqualTo(TicketStatus.VALIDATED);

            assertThat(ticket.getAmount())
                    .isEqualByComparingTo(ticketAmount);
        }
    }

    /**
     * Vérifie que deux soumissions simultanées du même ticket
     * n'attribuent les points qu'une seule fois.
     *
     * <p>Les deux appels utilisent exactement les mêmes données OCR.
     * Ils produisent donc le même fingerprint. La contrainte unique
     * sur le fingerprint doit empêcher le ticket d'être utilisé deux fois.</p>
     *
     * <p>Un seul traitement doit réussir. Le second doit échouer
     * et aucun double crédit ne doit être effectué.</p>
     *
     * @throws Exception si l'exécution concurrente échoue
     */
    @Test
    void shouldNotAwardPointsTwiceWhenSameTicketIsProcessedConcurrently()
            throws Exception {

        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID pointRuleId = UUID.randomUUID();

        byte[] image1 =
                "fake-image-1".getBytes();

        byte[] image2 =
                "fake-image-2".getBytes();

        OcrTicketData ocrData =
                createOcrTicketData(
                        "TICKET-1",
                        new BigDecimal("112.00")
                );

        when(ocrService.extractTicketData(image1))
                .thenReturn(ocrData);

        when(ocrService.extractTicketData(image2))
                .thenReturn(ocrData);

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

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        Callable<Boolean> task1 = () -> {
            try {
                Ticket ticket =
                        ticketService.extractTicketFromOcr(
                                image1,
                                merchantId,
                                customerId
                        );

                loyaltyService.addPointsFromTicket(
                        loyaltyId,
                        ticket
                );

                return true;

            } catch (Exception exception) {
                return false;
            }
        };

        Callable<Boolean> task2 = () -> {
            try {
                Ticket ticket =
                        ticketService.extractTicketFromOcr(
                                image2,
                                merchantId,
                                customerId
                        );

                loyaltyService.addPointsFromTicket(
                        loyaltyId,
                        ticket
                );

                return true;

            } catch (Exception exception) {
                return false;
            }
        };

        List<Future<Boolean>> futures =
                executorService.invokeAll(
                        List.of(task1, task2)
                );

        executorService.shutdown();

        List<Boolean> results = List.of(
                futures.get(0).get(),
                futures.get(1).get()
        );

        assertThat(results)
                .containsExactlyInAnyOrder(true, false);

        LoyaltyEntity loyalty =
                loyaltyRepository.findById(loyaltyId)
                        .orElseThrow();

        assertThat(loyalty.getPointsBalance())
                .isEqualTo(112);

        List<LoyaltyTransactionEntity> transactions =
                transactionRepository.findByLoyaltyId(loyaltyId);

        assertThat(transactions)
                .hasSize(1);

        List<TicketEntity> tickets =
                ticketRepository.findByMerchantId(merchantId);

        assertThat(tickets)
                .hasSize(1);

        TicketEntity ticket =
                tickets.get(0);

        assertThat(ticket.getTicketNumber())
                .isEqualTo("TICKET-1");

        assertThat(ticket.getCustomerId())
                .isEqualTo(customerId);

        assertThat(ticket.getMerchantId())
                .isEqualTo(merchantId);

        assertThat(ticket.getAmount())
                .isEqualByComparingTo("112.00");

        assertThat(ticket.getStatus())
                .isEqualTo(TicketStatus.VALIDATED);

        assertThat(ticket.getFingerprintHash())
                .isNotBlank();

        assertThat(transactions.get(0).getTicketId())
                .isEqualTo(ticket.getId());
    }

    /**
     * Crée les données qu'un service OCR aurait extraites
     * d'un ticket.
     *
     * @param ticketNumber numéro du ticket
     * @param amount montant du ticket
     * @return données OCR simulées
     */
    private OcrTicketData createOcrTicketData(
            String ticketNumber,
            BigDecimal amount
    ) {
        return new OcrTicketData(
                ticketNumber,
                LocalDate.of(
                        2026,
                        6,
                        15
                ),
                LocalTime.of(
                        12,
                        0
                ),
                amount,
                """
                RESTAURANT FIDELY
                Ticket: %s
                Date: 2026-06-15
                Heure: 12:00
                TOTAL: %s EUR
                """.formatted(
                        ticketNumber,
                        amount
                )
        );
    }

    /**
     * Crée une fidélité directement en base de données.
     *
     * @param id identifiant de la fidélité
     * @param customerId identifiant du client
     * @param merchantId identifiant du marchand
     */
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
                        LocalDateTime.of(
                                2026,
                                6,
                                15,
                                12,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                6,
                                15,
                                12,
                                0
                        )
                )
        );
    }

    /**
     * Crée une règle de points directement en base.
     *
     * @param id identifiant de la règle
     * @param merchantId identifiant du marchand
     */
    private void createPointRule(
            UUID id,
            UUID merchantId
    ) {
        pointRuleRepository.saveAndFlush(
                new PointRuleEntity(
                        id,
                        merchantId,
                        new BigDecimal("1.0000"),
                        roundingMethodValue(
                                RoundingMethod.FLOOR
                        ),
                        true,
                        LocalDateTime.of(
                                2026,
                                1,
                                1,
                                0,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                12,
                                31,
                                23,
                                59
                        ),
                        LocalDateTime.of(
                                2026,
                                1,
                                1,
                                0,
                                0
                        )
                )
        );
    }

    /**
     * Convertit la méthode d'arrondi du domaine en valeur
     * persistée en base de données.
     *
     * @param roundingMethod méthode d'arrondi
     * @return valeur persistée
     */
    private short roundingMethodValue(
            RoundingMethod roundingMethod
    ) {
        return switch (roundingMethod) {
            case FLOOR -> 0;
            case ROUND -> 1;
            case CEIL -> 2;
        };
    }

    /**
     * Insère un marchand de test.
     *
     * @param id identifiant du marchand
     */
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

    /**
     * Insère un client de test.
     *
     * @param id identifiant du client
     */
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