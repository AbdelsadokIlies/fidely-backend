package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.IAuthIdentityRepository;
import com.fidely.backend.application.port.out.IEmailSender;
import com.fidely.backend.application.port.out.IPasswordResetTokenRepository;
import com.fidely.backend.application.port.out.IPasswordResetTokenRepository.PasswordResetTokenData;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.security.IPasswordHasher;
import com.fidely.backend.application.port.out.security.IRefreshTokenGenerator;
import com.fidely.backend.application.port.out.security.ITokenHasher;
import com.fidely.backend.domain.models.auth.AuthIdentity;
import com.fidely.backend.domain.models.users.Customer;
import com.fidely.backend.domain.models.users.User;
import com.fidely.backend.infrastructure.config.ApiUrlProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IAuthIdentityRepository authIdentityRepository;

    @Mock
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private IPasswordHasher passwordHasher;

    @Mock
    private ITokenHasher tokenHasher;

    @Mock
    private IRefreshTokenGenerator tokenGenerator;

    @Mock
    private IEmailSender emailSender;

    private PasswordResetService service;

    private ApiUrlProperties apiUrlProperties;

    @BeforeEach
    void setUp() {
        apiUrlProperties = new ApiUrlProperties();
        apiUrlProperties.setUrl("http://localhost:8080/");

        service = new PasswordResetService(
                userRepository,
                authIdentityRepository,
                passwordResetTokenRepository,
                passwordHasher,
                tokenHasher,
                tokenGenerator,
                emailSender,
                apiUrlProperties
        );
    }

    @Test
    void shouldCreatePasswordResetToken() {
        UUID userId = UUID.randomUUID();
        String email = "reset@example.com";
        String rawToken = "raw-reset-token";
        String tokenHash = "hashed-reset-token";

        LocalDateTime before = LocalDateTime.now();

        User user = new Customer(
                userId,
                email,
                "Reset",
                "Test",
                "+33123456789",
                false,
                true,
                null,
                before,
                before
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(tokenGenerator.generate())
                .thenReturn(rawToken);

        when(tokenHasher.hash(rawToken))
                .thenReturn(tokenHash);

        String result = service.requestPasswordReset(email);

        LocalDateTime after = LocalDateTime.now();

        assertThat(result)
                .isEqualTo(rawToken);

        verify(passwordResetTokenRepository)
                .invalidateByUserId(
                        eq(userId),
                        any(LocalDateTime.class)
                );

        verify(tokenGenerator)
                .generate();

        verify(tokenHasher)
                .hash(rawToken);

        ArgumentCaptor<PasswordResetTokenData> captor =
                ArgumentCaptor.forClass(PasswordResetTokenData.class);

        verify(passwordResetTokenRepository)
                .save(captor.capture());

        PasswordResetTokenData savedToken =
                captor.getValue();

        assertThat(savedToken.id())
                .isNotNull();

        assertThat(savedToken.userId())
                .isEqualTo(userId);

        assertThat(savedToken.tokenHash())
                .isEqualTo(tokenHash);

        assertThat(savedToken.usedAt())
                .isNull();

        assertThat(savedToken.createdAt())
                .isBetween(before, after);

        assertThat(savedToken.expiresAt())
                .isEqualTo(
                        savedToken.createdAt().plusMinutes(15)
                );

        verify(emailSender)
                .send(
                        eq(email),
                        eq("Réinitialisation de votre mot de passe"),
                        contains(rawToken)
                );
    }

    @Test
    void shouldSendPasswordResetEmailWithResetUrl() {
        UUID userId = UUID.randomUUID();
        String email = "reset@example.com";
        String rawToken = "raw-reset-token";
        String tokenHash = "hashed-reset-token";

        User user = new Customer(
                userId,
                email,
                "Reset",
                "Test",
                "+33123456789",
                false,
                true,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(tokenGenerator.generate())
                .thenReturn(rawToken);

        when(tokenHasher.hash(rawToken))
                .thenReturn(tokenHash);

        service.requestPasswordReset(email);

        ArgumentCaptor<String> bodyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailSender)
                .send(
                        eq(email),
                        eq("Réinitialisation de votre mot de passe"),
                        bodyCaptor.capture()
                );

        String body = bodyCaptor.getValue();

        assertThat(body)
                .contains(
                        "http://localhost:8080/auth/reset-password?token="
                                + rawToken
                );
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(
                () -> service.requestPasswordReset(" ")
        )
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordResetTokenRepository);
        verifyNoInteractions(tokenGenerator);
        verifyNoInteractions(passwordHasher);
        verifyNoInteractions(tokenHasher);
        verifyNoInteractions(emailSender);
    }

    @Test
    void shouldRejectUnknownUser() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.requestPasswordReset(email)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(passwordResetTokenRepository);
        verifyNoInteractions(tokenGenerator);
        verifyNoInteractions(passwordHasher);
        verifyNoInteractions(tokenHasher);
        verifyNoInteractions(emailSender);
    }

    @Test
    void shouldInvalidatePreviousTokensBeforeCreatingNewToken() {
        UUID userId = UUID.randomUUID();
        String email = "reset@example.com";

        User user = new Customer(
                userId,
                email,
                "Reset",
                "Test",
                "+33123456789",
                false,
                true,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(tokenGenerator.generate())
                .thenReturn("new-reset-token");

        when(tokenHasher.hash("new-reset-token"))
                .thenReturn("hashed-new-reset-token");

        service.requestPasswordReset(email);

        verify(passwordResetTokenRepository)
                .invalidateByUserId(
                        eq(userId),
                        any(LocalDateTime.class)
                );

        verify(passwordResetTokenRepository)
                .save(any(PasswordResetTokenData.class));

        verify(tokenGenerator)
                .generate();

        verify(tokenHasher)
                .hash("new-reset-token");

        verify(emailSender)
                .send(
                        eq(email),
                        anyString(),
                        contains("new-reset-token")
                );
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        UUID identityId = UUID.randomUUID();

        String rawToken = "reset-token";
        String tokenHash = "hashed-reset-token";
        String newPassword = "NewPassword123!";
        String newPasswordHash = "hashed-new-password";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        PasswordResetTokenData resetToken =
                new PasswordResetTokenData(
                        tokenId,
                        userId,
                        tokenHash,
                        expiresAt,
                        null,
                        now
                );

        AuthIdentity identity = new AuthIdentity(
                identityId,
                userId,
                "password",
                null,
                "old-password-hash",
                now
        );

        when(tokenHasher.hash(rawToken))
                .thenReturn(tokenHash);

        when(passwordResetTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(resetToken));

        when(authIdentityRepository.findByUserIdAndProvider(
                userId,
                "password"
        )).thenReturn(Optional.of(identity));

        when(passwordHasher.hash(newPassword))
                .thenReturn(newPasswordHash);

        service.resetPassword(rawToken, newPassword);

        verify(tokenHasher)
                .hash(rawToken);

        verify(passwordResetTokenRepository)
                .findByTokenHash(tokenHash);

        verify(authIdentityRepository)
                .findByUserIdAndProvider(
                        userId,
                        "password"
                );

        verify(passwordHasher)
                .hash(newPassword);

        verify(authIdentityRepository)
                .save(any(AuthIdentity.class));

        verify(passwordResetTokenRepository)
                .markAsUsed(
                        eq(tokenId),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldRejectUnknownResetToken() {
        String rawToken = "unknown-token";
        String tokenHash = "hashed-unknown-token";

        when(tokenHasher.hash(rawToken))
                .thenReturn(tokenHash);

        when(passwordResetTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.resetPassword(
                        rawToken,
                        "NewPassword123!"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password reset token");

        verify(tokenHasher)
                .hash(rawToken);

        verify(passwordResetTokenRepository)
                .findByTokenHash(tokenHash);

        verifyNoInteractions(authIdentityRepository);
    }

    @Test
    void shouldRejectExpiredResetToken() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        PasswordResetTokenData resetToken =
                new PasswordResetTokenData(
                        tokenId,
                        userId,
                        "hashed-reset-token",
                        now.minusMinutes(1),
                        null,
                        now.minusMinutes(16)
                );

        when(tokenHasher.hash("reset-token"))
                .thenReturn("hashed-reset-token");

        when(passwordResetTokenRepository.findByTokenHash(
                "hashed-reset-token"
        )).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(
                () -> service.resetPassword(
                        "reset-token",
                        "NewPassword123!"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password reset token has expired");

        verifyNoInteractions(authIdentityRepository);

        verify(passwordResetTokenRepository, never())
                .markAsUsed(
                        any(UUID.class),
                        any(LocalDateTime.class)
                );
    }

    @Test
    void shouldRejectAlreadyUsedResetToken() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        PasswordResetTokenData resetToken =
                new PasswordResetTokenData(
                        tokenId,
                        userId,
                        "hashed-reset-token",
                        now.plusMinutes(15),
                        now.minusMinutes(1),
                        now.minusMinutes(16)
                );

        when(tokenHasher.hash("reset-token"))
                .thenReturn("hashed-reset-token");

        when(passwordResetTokenRepository.findByTokenHash(
                "hashed-reset-token"
        )).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(
                () -> service.resetPassword(
                        "reset-token",
                        "NewPassword123!"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password reset token has already been used");

        verifyNoInteractions(authIdentityRepository);

        verify(passwordResetTokenRepository, never())
                .markAsUsed(
                        any(UUID.class),
                        any(LocalDateTime.class)
                );
    }
}