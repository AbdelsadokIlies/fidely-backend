package com.fidely.backend.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de {@link BCryptPasswordHasher}.
 */
class BCryptPasswordHasherTest {

    private BCryptPasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new BCryptPasswordHasher();
    }

    @Test
    void shouldHashPassword() {
        String rawPassword = "MySecurePassword123!";

        String hashedPassword = passwordHasher.hash(rawPassword);

        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(
                hashedPassword.startsWith("$2")
        );
    }

    @Test
    void shouldMatchCorrectPassword() {
        String rawPassword = "MySecurePassword123!";
        String hashedPassword = passwordHasher.hash(rawPassword);

        boolean result = passwordHasher.matches(
                rawPassword,
                hashedPassword
        );

        assertTrue(result);
    }

    @Test
    void shouldNotMatchIncorrectPassword() {
        String hashedPassword =
                passwordHasher.hash("MySecurePassword123!");

        boolean result = passwordHasher.matches(
                "WrongPassword123!",
                hashedPassword
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenRawPasswordIsNull() {
        String hashedPassword =
                passwordHasher.hash("MySecurePassword123!");

        boolean result = passwordHasher.matches(
                null,
                hashedPassword
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenRawPasswordIsBlank() {
        String hashedPassword =
                passwordHasher.hash("MySecurePassword123!");

        boolean result = passwordHasher.matches(
                "   ",
                hashedPassword
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenHashedPasswordIsNull() {
        boolean result = passwordHasher.matches(
                "MySecurePassword123!",
                null
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenHashedPasswordIsBlank() {
        boolean result = passwordHasher.matches(
                "MySecurePassword123!",
                "   "
        );

        assertFalse(result);
    }

    @Test
    void shouldRejectNullPasswordWhenHashing() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> passwordHasher.hash(null)
        );
    }

    @Test
    void shouldRejectBlankPasswordWhenHashing() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> passwordHasher.hash("   ")
        );
    }
}