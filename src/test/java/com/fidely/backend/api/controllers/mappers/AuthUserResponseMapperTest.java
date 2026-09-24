package com.fidely.backend.api.controllers.mappers;

import com.fidely.backend.api.dtos.mappers.auth.AuthUserResponseMapper;
import com.fidely.backend.api.dtos.models.auth.AuthUserResponse;
import com.fidely.backend.domain.models.users.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests unitaires de {@link AuthUserResponseMapper}.
 */
class AuthUserResponseMapperTest {

    private AuthUserResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuthUserResponseMapper();
    }

    @Test
    void shouldMapUserToResponse() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer(
                userId,
                "john@example.com",
                "John",
                "Doe",
                "0612345678",
                true,
                true,
                LocalDate.of(1998, 5, 20),
                now,
                now
        );

        AuthUserResponse response = mapper.toResponse(customer);

        assertEquals(userId, response.id());
        assertEquals("john@example.com", response.email());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals(true, response.emailVerified());
    }

    @Test
    void shouldMapUnverifiedUserToResponse() {
        Customer customer = new Customer(
                UUID.randomUUID(),
                "john@example.com",
                "John",
                "Doe",
                null,
                false,
                true,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        AuthUserResponse response = mapper.toResponse(customer);

        assertEquals(false, response.emailVerified());
    }

    @Test
    void shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toResponse(null)
        );
    }
}