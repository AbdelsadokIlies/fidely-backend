package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IPasswordResetTokenRepository.PasswordResetTokenData;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataPasswordResetTokenRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataUserRepository;
import com.fidely.backend.infrastructure.entities.models.auth.PasswordResetTokenEntity;
import com.fidely.backend.infrastructure.entities.models.users.UserEntity;
import com.fidely.backend.infrastructure.entities.mappers.auth.PasswordResetTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class PasswordResetTokenRepositoryAdapterTest {

    @Autowired
    private SpringDataPasswordResetTokenRepository springDataRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    private PasswordResetTokenRepositoryAdapter adapter;

    private final PasswordResetTokenMapper mapper =
            new PasswordResetTokenMapper();

    @BeforeEach
    void setUp() {
        adapter = new PasswordResetTokenRepositoryAdapter(
                springDataRepository,
                mapper
        );
    }

    @Test
    void shouldSaveAndFindByTokenHash() {
        PasswordResetTokenData token = createToken();

        adapter.save(token);

        Optional<PasswordResetTokenData> result =
                adapter.findByTokenHash(token.tokenHash());

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(token.id());
        assertThat(result.get().userId()).isEqualTo(token.userId());
        assertThat(result.get().tokenHash())
                .isEqualTo(token.tokenHash());
        assertThat(result.get().expiresAt())
                .isEqualTo(token.expiresAt());
        assertThat(result.get().usedAt())
                .isNull();
    }

    @Test
    void shouldInvalidateTokensByUserId() {
        UUID userId = createUser().getId();

        PasswordResetTokenData first = createToken(
                userId,
                "hash-1"
        );

        PasswordResetTokenData second = createToken(
                userId,
                "hash-2"
        );

        adapter.save(first);
        adapter.save(second);

        LocalDateTime usedAt =
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        adapter.invalidateByUserId(userId, usedAt);

        Optional<PasswordResetTokenData> firstResult =
                adapter.findByTokenHash(first.tokenHash());

        Optional<PasswordResetTokenData> secondResult =
                adapter.findByTokenHash(second.tokenHash());

        assertThat(firstResult).isPresent();
        assertThat(secondResult).isPresent();

        assertThat(firstResult.get().usedAt())
                .isEqualTo(usedAt);

        assertThat(secondResult.get().usedAt())
                .isEqualTo(usedAt);
    }

    @Test
    void shouldMarkTokenAsUsed() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        LocalDateTime createdAt = LocalDateTime.now()
                .truncatedTo(ChronoUnit.MICROS);

        LocalDateTime expiresAt = createdAt.plusMinutes(15);

        LocalDateTime usedAt = createdAt.plusMinutes(1);

        UserEntity user = new UserEntity(
                userId,
                "reset-test@example.com",
                "Reset",
                "Test",
                false,
                true,
                createdAt,
                createdAt
        );

        userRepository.save(user);

        PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                tokenId,
                userId,
                "hashed-token",
                expiresAt,
                null,
                createdAt
        );

        springDataRepository.save(token);

        adapter.markAsUsed(tokenId, usedAt);

        PasswordResetTokenEntity persistedToken =
                springDataRepository.findById(tokenId)
                        .orElseThrow();

        assertThat(persistedToken.getUsedAt())
                .isEqualTo(usedAt);
    }

    @Test
    void shouldNotInvalidateAlreadyUsedTokens() {
        UUID userId = createUser().getId();

        LocalDateTime createdAt =
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        LocalDateTime originalUsedAt =
                createdAt.plusMinutes(1);

        PasswordResetTokenData usedToken =
                new PasswordResetTokenData(
                        UUID.randomUUID(),
                        userId,
                        "already-used-" + UUID.randomUUID(),
                        createdAt.plusMinutes(30),
                        originalUsedAt,
                        createdAt
                );

        adapter.save(usedToken);

        LocalDateTime newUsedAt =
                createdAt.plusMinutes(2);

        adapter.invalidateByUserId(userId, newUsedAt);

        Optional<PasswordResetTokenData> result =
                adapter.findByTokenHash(usedToken.tokenHash());

        assertThat(result).isPresent();
        assertThat(result.get().usedAt())
                .isEqualTo(originalUsedAt);
    }

    @Test
    void shouldDeleteById() {
        PasswordResetTokenData token = createToken();

        adapter.save(token);

        adapter.deleteById(token.id());

        assertThat(adapter.findByTokenHash(token.tokenHash()))
                .isEmpty();
    }

    @Test
    void shouldNotMarkAlreadyUsedTokenAsUsedAgain() {
        UUID userId = createUser().getId();

        LocalDateTime createdAt =
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        LocalDateTime originalUsedAt =
                createdAt.plusMinutes(1);

        PasswordResetTokenData usedToken =
                new PasswordResetTokenData(
                        UUID.randomUUID(),
                        userId,
                        "already-used-" + UUID.randomUUID(),
                        createdAt.plusMinutes(30),
                        originalUsedAt,
                        createdAt
                );

        adapter.save(usedToken);

        LocalDateTime newUsedAt =
                createdAt.plusMinutes(2);

        adapter.markAsUsed(
                usedToken.id(),
                newUsedAt
        );

        Optional<PasswordResetTokenData> result =
                adapter.findByTokenHash(usedToken.tokenHash());

        assertThat(result).isPresent();

        assertThat(result.get().usedAt())
                .isEqualTo(originalUsedAt);
    }

    private PasswordResetTokenData createToken() {
        UUID userId = createUser().getId();

        return createToken(
                userId,
                "hash-" + UUID.randomUUID()
        );
    }

    private PasswordResetTokenData createToken(
            UUID userId,
            String tokenHash
    ) {
        LocalDateTime createdAt =
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        return new PasswordResetTokenData(
                UUID.randomUUID(),
                userId,
                tokenHash,
                createdAt.plusMinutes(30),
                null,
                createdAt
        );
    }

    private UserEntity createUser() {
        LocalDateTime now =
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                "test-" + UUID.randomUUID() + "@example.com",
                "Test",
                "User",
                false,
                true,
                now,
                now
        );

        return userRepository.save(user);
    }
}
