package com.fidely.backend.api.controllers.mappers;

import com.fidely.backend.api.dtos.mappers.auth.LoginResponseMapper;
import com.fidely.backend.api.dtos.models.auth.LoginResponse;
import com.fidely.backend.domain.models.users.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de {@link LoginResponseMapper}.
 */
class LoginResponseMapperTest {

    private LoginResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new LoginResponseMapper();
    }

    @Test
    void shouldMapUserToLoginResponse() {
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
                null,
                now,
                now
        );

        LoginResponse response = mapper.toResponse(customer);

        assertEquals(userId, response.id());
        assertEquals("john@example.com", response.email());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertTrue(response.emailVerified());
    }

    @Test
    void shouldMapUnverifiedUserToLoginResponse() {
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

        LoginResponse response = mapper.toResponse(customer);

        assertFalse(response.emailVerified());
    }

    @Test
    void shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toResponse(null)
        );
    }
}