package com.fidely.backend.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Sha256TokenHasherTest {

    private Sha256TokenHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new Sha256TokenHasher();
    }

    @Test
    void shouldHashToken() {
        String token = "my-token";

        String hash = hasher.hash(token);

        assertThat(hash)
                .isNotNull()
                .isNotBlank()
                .hasSize(64);
    }

    @Test
    void shouldProduceSameHashForSameToken() {
        String token = "my-token";

        String firstHash = hasher.hash(token);
        String secondHash = hasher.hash(token);

        assertThat(firstHash)
                .isEqualTo(secondHash);
    }

    @Test
    void shouldProduceDifferentHashesForDifferentTokens() {
        String firstHash = hasher.hash("first-token");
        String secondHash = hasher.hash("second-token");

        assertThat(firstHash)
                .isNotEqualTo(secondHash);
    }

    @Test
    void shouldMatchTokenWithItsHash() {
        String token = "my-token";

        String hash = hasher.hash(token);

        assertThat(hasher.matches(token, hash))
                .isTrue();
    }

    @Test
    void shouldRejectTokenWithDifferentHash() {
        String token = "my-token";
        String otherToken = "other-token";

        String hash = hasher.hash(token);

        assertThat(hasher.matches(otherToken, hash))
                .isFalse();
    }

    @Test
    void shouldRejectNullToken() {
        assertThatThrownBy(
                () -> hasher.hash(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token cannot be null or blank");
    }

    @Test
    void shouldRejectBlankToken() {
        assertThatThrownBy(
                () -> hasher.hash("   ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token cannot be null or blank");
    }

    @Test
    void shouldReturnFalseWhenMatchingNullToken() {
        String hash = hasher.hash("my-token");

        assertThat(hasher.matches(null, hash))
                .isFalse();
    }

    @Test
    void shouldReturnFalseWhenMatchingBlankToken() {
        String hash = hasher.hash("my-token");

        assertThat(hasher.matches("   ", hash))
                .isFalse();
    }

    @Test
    void shouldReturnFalseWhenMatchingNullHash() {
        assertThat(hasher.matches("my-token", null))
                .isFalse();
    }

    @Test
    void shouldReturnFalseWhenMatchingBlankHash() {
        assertThat(hasher.matches("my-token", "   "))
                .isFalse();
    }
}