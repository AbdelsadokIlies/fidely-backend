package com.fidely.backend.model.loyalty;

import com.fidely.backend.domain.model.Merchants.Merchant;
import com.fidely.backend.domain.model.loyalty.Loyalty;
import com.fidely.backend.domain.model.users.Customer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LoyaltyTest {

    private final Customer customer = mock(Customer.class);
    private final Merchant merchant = mock(Merchant.class);

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------

    @Test
    void shouldCreateLoyaltyWithValidData() {

        LocalDateTime createdAt = LocalDateTime.of(
                2026, 1, 1, 10, 0
        );

        LocalDateTime updatedAt = LocalDateTime.of(
                2026, 1, 1, 10, 0
        );

        Loyalty loyalty = createLoyalty(
                100,
                createdAt,
                updatedAt
        );

        assertEquals(100, loyalty.getPointsBalance());
        assertEquals(customer, loyalty.getCustomer());
        assertEquals(merchant, loyalty.getMerchant());
        assertEquals(createdAt, loyalty.getCreatedAt());
        assertEquals(updatedAt, loyalty.getUpdatedAt());
    }

    @Test
    void shouldAllowZeroInitialBalance() {

        Loyalty loyalty = createLoyalty(
                0,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        assertEquals(0, loyalty.getPointsBalance());
    }

    @Test
    void shouldRejectNegativeInitialBalance() {

        assertThrows(
                IllegalArgumentException.class,
                () -> createLoyalty(
                        -1,
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        LocalDateTime.of(2026, 1, 1, 10, 0)
                )
        );
    }

    @Test
    void shouldRejectNullCustomer() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Loyalty(
                        UUID.randomUUID(),
                        null,
                        merchant,
                        100,
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        LocalDateTime.of(2026, 1, 1, 10, 0)
                )
        );
    }

    @Test
    void shouldRejectNullMerchant() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Loyalty(
                        UUID.randomUUID(),
                        customer,
                        null,
                        100,
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        LocalDateTime.of(2026, 1, 1, 10, 0)
                )
        );
    }

    @Test
    void shouldRejectNullCreatedAt() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Loyalty(
                        UUID.randomUUID(),
                        customer,
                        merchant,
                        100,
                        null,
                        LocalDateTime.of(2026, 1, 1, 10, 0)
                )
        );
    }

    @Test
    void shouldRejectNullUpdatedAt() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Loyalty(
                        UUID.randomUUID(),
                        customer,
                        merchant,
                        100,
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        null
                )
        );
    }

    @Test
    void shouldRejectUpdatedAtBeforeCreatedAt() {

        LocalDateTime createdAt = LocalDateTime.of(
                2026, 1, 2, 10, 0
        );

        LocalDateTime updatedAt = LocalDateTime.of(
                2026, 1, 1, 10, 0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> createLoyalty(
                        100,
                        createdAt,
                        updatedAt
                )
        );
    }


    // ---------------------------------------------------------
    // addPoints()
    // ---------------------------------------------------------

    @Test
    void shouldAddPoints() {

        Loyalty loyalty = createLoyalty(100);

        loyalty.addPoints(50);

        assertEquals(150, loyalty.getPointsBalance());
    }

    @Test
    void shouldAddPointsToZeroBalance() {

        Loyalty loyalty = createLoyalty(0);

        loyalty.addPoints(100);

        assertEquals(100, loyalty.getPointsBalance());
    }

    @Test
    void shouldRejectZeroPointsWhenAdding() {

        Loyalty loyalty = createLoyalty(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> loyalty.addPoints(0)
        );

        assertEquals(100, loyalty.getPointsBalance());
    }

    @Test
    void shouldRejectNegativePointsWhenAdding() {

        Loyalty loyalty = createLoyalty(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> loyalty.addPoints(-50)
        );

        assertEquals(100, loyalty.getPointsBalance());
    }


    // ---------------------------------------------------------
    // removePoints()
    // ---------------------------------------------------------

    @Test
    void shouldRemovePoints() {

        Loyalty loyalty = createLoyalty(100);

        loyalty.removePoints(40);

        assertEquals(60, loyalty.getPointsBalance());
    }

    @Test
    void shouldRemoveAllPoints() {

        Loyalty loyalty = createLoyalty(100);

        loyalty.removePoints(100);

        assertEquals(0, loyalty.getPointsBalance());
    }

    @Test
    void shouldRejectZeroPointsWhenRemoving() {

        Loyalty loyalty = createLoyalty(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> loyalty.removePoints(0)
        );

        assertEquals(100, loyalty.getPointsBalance());
    }

    @Test
    void shouldRejectNegativePointsWhenRemoving() {

        Loyalty loyalty = createLoyalty(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> loyalty.removePoints(-50)
        );

        assertEquals(100, loyalty.getPointsBalance());
    }

    @Test
    void shouldRejectRemovingMorePointsThanBalance() {

        Loyalty loyalty = createLoyalty(100);

        assertThrows(
                IllegalStateException.class,
                () -> loyalty.removePoints(101)
        );

        assertEquals(100, loyalty.getPointsBalance());
    }


    // ---------------------------------------------------------
    // updatedAt
    // ---------------------------------------------------------

    @Test
    void shouldUpdateUpdatedAtWhenAddingPoints() throws InterruptedException {

        Loyalty loyalty = createLoyalty(100);

        LocalDateTime before = loyalty.getUpdatedAt();

        Thread.sleep(10);

        loyalty.addPoints(50);

        LocalDateTime after = loyalty.getUpdatedAt();

        assertTrue(after.isAfter(before));
    }

    @Test
    void shouldUpdateUpdatedAtWhenRemovingPoints() throws InterruptedException {

        Loyalty loyalty = createLoyalty(100);

        LocalDateTime before = loyalty.getUpdatedAt();

        Thread.sleep(10);

        loyalty.removePoints(50);

        LocalDateTime after = loyalty.getUpdatedAt();

        assertTrue(after.isAfter(before));
    }

    @Test
    void shouldNotUpdateUpdatedAtWhenAddingInvalidPoints() {

        Loyalty loyalty = createLoyalty(100);

        LocalDateTime before = loyalty.getUpdatedAt();

        assertThrows(
                IllegalArgumentException.class,
                () -> loyalty.addPoints(0)
        );

        assertEquals(before, loyalty.getUpdatedAt());
    }

    @Test
    void shouldNotUpdateUpdatedAtWhenRemovingInvalidPoints() {

        Loyalty loyalty = createLoyalty(100);

        LocalDateTime before = loyalty.getUpdatedAt();

        assertThrows(
                IllegalStateException.class,
                () -> loyalty.removePoints(101)
        );

        assertEquals(before, loyalty.getUpdatedAt());
    }


    // ---------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------

    private Loyalty createLoyalty(int pointsBalance) {

        LocalDateTime now = LocalDateTime.now();

        return createLoyalty(
                pointsBalance,
                now,
                now
        );
    }

    private Loyalty createLoyalty(
            int pointsBalance,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Loyalty(
                UUID.randomUUID(),
                customer,
                merchant,
                pointsBalance,
                createdAt,
                updatedAt
        );
    }
}