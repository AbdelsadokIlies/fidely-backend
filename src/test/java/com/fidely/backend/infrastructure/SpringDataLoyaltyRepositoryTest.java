package com.fidely.backend.infrastructure;

import com.fidely.backend.IntegrationTest;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyRepository;
import com.fidely.backend.infrastructure.entities.models.loyalties.LoyaltyEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class SpringDataLoyaltyRepositoryTest extends IntegrationTest {

    @Autowired
    private SpringDataLoyaltyRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndFindLoyaltyById() {
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        LoyaltyEntity loyalty = createLoyalty(
                loyaltyId,
                customerId,
                merchantId,
                100
        );

        repository.saveAndFlush(loyalty);

        Optional<LoyaltyEntity> result =
                repository.findById(loyaltyId);

        assertThat(result)
                .isPresent()
                .get()
                .extracting(LoyaltyEntity::getPointsBalance)
                .isEqualTo(100);
    }

    @Test
    void shouldFindByCustomerIdAndMerchantId() {
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        repository.saveAndFlush(
                createLoyalty(
                        loyaltyId,
                        customerId,
                        merchantId,
                        150
                )
        );

        Optional<LoyaltyEntity> result =
                repository.findByCustomerIdAndMerchantId(
                        customerId,
                        merchantId
                );

        assertThat(result)
                .isPresent()
                .get()
                .extracting(LoyaltyEntity::getId)
                .isEqualTo(loyaltyId);
    }

    @Test
    void shouldReturnEmptyWhenCustomerAndMerchantDoNotMatch() {
        UUID loyaltyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        UUID otherCustomerId = UUID.randomUUID();
        UUID otherMerchantId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        insertMerchant(otherMerchantId);
        insertCustomer(otherCustomerId);

        repository.saveAndFlush(
                createLoyalty(
                        loyaltyId,
                        customerId,
                        merchantId,
                        100
                )
        );

        Optional<LoyaltyEntity> result =
                repository.findByCustomerIdAndMerchantId(
                        otherCustomerId,
                        otherMerchantId
                );

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldFindLoyaltiesByCustomerId() {
        UUID customerId = UUID.randomUUID();

        UUID merchantId1 = UUID.randomUUID();
        UUID merchantId2 = UUID.randomUUID();

        insertCustomer(customerId);
        insertMerchant(merchantId1);
        insertMerchant(merchantId2);

        repository.saveAndFlush(
                createLoyalty(
                        UUID.randomUUID(),
                        customerId,
                        merchantId1,
                        100
                )
        );

        repository.saveAndFlush(
                createLoyalty(
                        UUID.randomUUID(),
                        customerId,
                        merchantId2,
                        200
                )
        );

        List<LoyaltyEntity> result =
                repository.findByCustomerId(customerId);

        assertThat(result)
                .hasSize(2)
                .extracting(LoyaltyEntity::getCustomerId)
                .containsOnly(customerId);
    }

    @Test
    void shouldReturnEmptyWhenCustomerHasNoLoyalties() {
        List<LoyaltyEntity> result =
                repository.findByCustomerId(UUID.randomUUID());

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldFindLoyaltiesByMerchantId() {
        UUID merchantId = UUID.randomUUID();

        UUID customerId1 = UUID.randomUUID();
        UUID customerId2 = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId1);
        insertCustomer(customerId2);

        repository.saveAndFlush(
                createLoyalty(
                        UUID.randomUUID(),
                        customerId1,
                        merchantId,
                        100
                )
        );

        repository.saveAndFlush(
                createLoyalty(
                        UUID.randomUUID(),
                        customerId2,
                        merchantId,
                        200
                )
        );

        List<LoyaltyEntity> result =
                repository.findByMerchantId(merchantId);

        assertThat(result)
                .hasSize(2)
                .extracting(LoyaltyEntity::getMerchantId)
                .containsOnly(merchantId);
    }

    @Test
    void shouldReturnEmptyWhenMerchantHasNoLoyalties() {
        List<LoyaltyEntity> result =
                repository.findByMerchantId(UUID.randomUUID());

        assertThat(result)
                .isEmpty();
    }

    @Test
    void shouldRejectDuplicateCustomerAndMerchant() {
        UUID customerId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        insertMerchant(merchantId);
        insertCustomer(customerId);

        repository.saveAndFlush(
                createLoyalty(
                        UUID.randomUUID(),
                        customerId,
                        merchantId,
                        100
                )
        );

        LoyaltyEntity duplicate =
                createLoyalty(
                        UUID.randomUUID(),
                        customerId,
                        merchantId,
                        200
                );

        assertThat(
                org.assertj.core.api.Assertions.catchThrowable(
                        () -> repository.saveAndFlush(duplicate)
                )
        )
                .isInstanceOf(
                        org.springframework.dao.DataIntegrityViolationException.class
                );
    }

    private LoyaltyEntity createLoyalty(
            UUID id,
            UUID customerId,
            UUID merchantId,
            int pointsBalance
    ) {
        LocalDateTime now =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        return new LoyaltyEntity(
                id,
                customerId,
                merchantId,
                pointsBalance,
                now,
                now
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
                        "VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO NOTHING",
                id,
                id + "@test.com",
                "Test",
                "User"
        );
    }
}
