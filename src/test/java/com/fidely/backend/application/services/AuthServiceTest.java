package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IAuthService;
import com.fidely.backend.application.port.in.IRefreshSessionService;
import com.fidely.backend.application.port.out.IAuthIdentityRepository;
import com.fidely.backend.application.port.out.IEmailSender;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.security.IAccessTokenManagement;
import com.fidely.backend.application.port.out.security.IEmailVerificationTokenGenerator;
import com.fidely.backend.application.port.out.security.IPasswordHasher;
import com.fidely.backend.domain.models.auth.AuthIdentity;
import com.fidely.backend.domain.models.auth.RefreshSession;
import com.fidely.backend.domain.models.users.Customer;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;
import com.fidely.backend.infrastructure.config.ApiUrlProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**

 * Tests unitaires de {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IAuthIdentityRepository authIdentityRepository;

    @Mock
    private IPasswordHasher passwordHasher;

    @Mock
    private IAccessTokenManagement accessTokenGenerator;

    @Mock
    private IRefreshSessionService refreshSessionService;

    @Mock
    private IEmailSender emailSender;

    @Mock
    private IEmailVerificationTokenGenerator emailVerificationTokenGenerator;

    @Mock
    private ApiUrlProperties apiUrlProperties;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                authIdentityRepository,
                passwordHasher,
                accessTokenGenerator,
                refreshSessionService,
                emailSender,
                emailVerificationTokenGenerator,
                apiUrlProperties
        );
    }

    @Test
    void shouldRegisterCustomer() {
        String email = "[customer@example.com](mailto:customer@example.com)";
        String password = "Password123!";
        String hashedPassword = "hashed-password";
        String verificationToken = "verification-token";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        when(passwordHasher.hash(password))
                .thenReturn(hashedPassword);

        when(emailVerificationTokenGenerator.generate(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(verificationToken);

        when(apiUrlProperties.getUrl())
                .thenReturn("http://test-api/");

        Customer result = authService.registerCustomer(
                email,
                "John",
                "Doe",
                "0612345678",
                LocalDate.of(1998, 5, 20),
                password
        );

        assertEquals(email, result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("0612345678", result.getPhone());
        assertEquals(
                LocalDate.of(1998, 5, 20),
                result.getBirthDate()
        );
        assertFalse(result.isEmailVerified());
        assertTrue(result.isActive());

        verify(userRepository).findByEmail(email);
        verify(userRepository).save(result);
        verify(passwordHasher).hash(password);

        ArgumentCaptor<AuthIdentity> identityCaptor =
                ArgumentCaptor.forClass(AuthIdentity.class);

        verify(authIdentityRepository).save(identityCaptor.capture());

        AuthIdentity identity = identityCaptor.getValue();

        assertEquals(result.getId(), identity.getUserId());
        assertEquals("email", identity.getProvider());
        assertEquals(hashedPassword, identity.getPasswordHash());
        assertNull(identity.getProviderUserId());

        verify(emailVerificationTokenGenerator)
                .generate(result.getId());

        verify(apiUrlProperties).getUrl();

        verify(emailSender).send(
                org.mockito.ArgumentMatchers.eq(email),
                org.mockito.ArgumentMatchers.eq(
                        "Vérification de votre adresse email"
                ),
                org.mockito.ArgumentMatchers.contains(
                        "http://test-api/auth/verify-email?token="
                                + verificationToken
                )
        );

    }

    @Test
    void shouldRegisterMerchantManager() {
        UUID merchantId = UUID.randomUUID();
        String email = "[manager@example.com](mailto:manager@example.com)";
        String password = "Password123!";
        String hashedPassword = "hashed-password";
        String verificationToken = "verification-token";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        when(passwordHasher.hash(password))
                .thenReturn(hashedPassword);

        when(emailVerificationTokenGenerator.generate(
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(verificationToken);

        when(apiUrlProperties.getUrl())
                .thenReturn("http://test-api/");

        MerchantManager result =
                authService.registerMerchantManager(
                        merchantId,
                        email,
                        "Jane",
                        "Smith",
                        password
                );

        assertEquals(merchantId, result.getMerchantId());
        assertEquals(email, result.getEmail());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertFalse(result.isEmailVerified());
        assertTrue(result.isActive());

        verify(userRepository).findByEmail(email);
        verify(userRepository).save(result);
        verify(passwordHasher).hash(password);

        ArgumentCaptor<AuthIdentity> identityCaptor =
                ArgumentCaptor.forClass(AuthIdentity.class);

        verify(authIdentityRepository).save(identityCaptor.capture());

        AuthIdentity identity = identityCaptor.getValue();

        assertEquals(result.getId(), identity.getUserId());
        assertEquals("email", identity.getProvider());
        assertEquals(hashedPassword, identity.getPasswordHash());
        assertNull(identity.getProviderUserId());

        verify(emailVerificationTokenGenerator)
                .generate(result.getId());

        verify(apiUrlProperties).getUrl();

        verify(emailSender).send(
                org.mockito.ArgumentMatchers.eq(email),
                org.mockito.ArgumentMatchers.eq(
                        "Vérification de votre adresse email"
                ),
                org.mockito.ArgumentMatchers.contains(
                        "http://test-api/auth/verify-email?token="
                                + verificationToken
                )
        );

    }

    @Test
    void shouldVerifyCustomerEmail() {
        Customer customer = createCustomerEmailNotVerified();

        String verificationToken = "verification-token";

        when(emailVerificationTokenGenerator.extractUserId(
                verificationToken
        )).thenReturn(customer.getId());

        when(userRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        authService.verifyEmail(verificationToken);

        assertTrue(customer.isEmailVerified());

        verify(emailVerificationTokenGenerator)
                .extractUserId(verificationToken);

        verify(userRepository).findById(customer.getId());
        verify(userRepository).save(customer);

    }

    @Test
    void shouldNotSaveCustomerWhenEmailIsAlreadyVerified() {
        Customer customer = createCustomer();

        String verificationToken = "verification-token";

        when(emailVerificationTokenGenerator.extractUserId(
                verificationToken
        )).thenReturn(customer.getId());

        when(userRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        authService.verifyEmail(verificationToken);

        assertTrue(customer.isEmailVerified());

        verify(emailVerificationTokenGenerator)
                .extractUserId(verificationToken);

        verify(userRepository).findById(customer.getId());

        verify(userRepository, never()).save(customer);

    }

    @Test
    void shouldRejectEmailVerificationWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        String verificationToken = "verification-token";

        when(emailVerificationTokenGenerator.extractUserId(
                verificationToken
        )).thenReturn(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> authService.verifyEmail(verificationToken)
        );

        verify(emailVerificationTokenGenerator)
                .extractUserId(verificationToken);

        verify(userRepository).findById(userId);

    }

    @Test
    void shouldResendVerificationEmail() {
        Customer customer = createCustomerEmailNotVerified();

        String email = customer.getEmail();
        String verificationToken = "verification-token";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(customer));

        when(emailVerificationTokenGenerator.generate(
                customer.getId()
        )).thenReturn(verificationToken);

        when(apiUrlProperties.getUrl())
                .thenReturn("http://test-api/");

        authService.resendVerificationEmail(email);

        verify(userRepository).findByEmail(email);

        verify(emailVerificationTokenGenerator)
                .generate(customer.getId());

        verify(apiUrlProperties).getUrl();

        verify(emailSender).send(
                org.mockito.ArgumentMatchers.eq(email),
                org.mockito.ArgumentMatchers.eq(
                        "Vérification de votre adresse email"
                ),
                org.mockito.ArgumentMatchers.contains(
                        "http://test-api/auth/verify-email?token="
                                + verificationToken
                )
        );
    }

    @Test
    void shouldRejectResendVerificationEmailWhenEmailIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.resendVerificationEmail("   ")
        );

        verifyNoInteractions(
                userRepository,
                emailVerificationTokenGenerator,
                apiUrlProperties,
                emailSender
        );
    }

    @Test
    void shouldRejectResendVerificationEmailWhenUserDoesNotExist() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.resendVerificationEmail(email)
        );

        verify(userRepository).findByEmail(email);

        verifyNoInteractions(
                emailVerificationTokenGenerator,
                apiUrlProperties,
                emailSender
        );
    }

    @Test
    void shouldRejectResendVerificationEmailWhenEmailIsAlreadyVerified() {
        Customer customer = createCustomer();

        String email = customer.getEmail();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(customer));

        assertThrows(
                IllegalStateException.class,
                () -> authService.resendVerificationEmail(email)
        );

        verify(userRepository).findByEmail(email);

        verifyNoInteractions(
                emailVerificationTokenGenerator,
                apiUrlProperties,
                emailSender
        );
    }

    @Test
    void shouldRejectCustomerRegistrationWhenEmailAlreadyExists() {
        String email = "[existing@example.com](mailto:existing@example.com)";

        User existingUser = createCustomer();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                IllegalStateException.class,
                () -> authService.registerCustomer(
                        email,
                        "John",
                        "Doe",
                        null,
                        null,
                        "Password123!"
                )
        );

        verify(userRepository).findByEmail(email);

        verify(userRepository, never()).save(
                org.mockito.ArgumentMatchers.any()
        );

        verify(authIdentityRepository, never()).save(
                org.mockito.ArgumentMatchers.any()
        );

        verify(passwordHasher, never()).hash(
                org.mockito.ArgumentMatchers.any()
        );

        verifyNoInteractions(
                emailVerificationTokenGenerator,
                apiUrlProperties,
                emailSender
        );

    }

    @Test
    void shouldRejectMerchantRegistrationWhenEmailAlreadyExists() {
        String email = "[existing@example.com](mailto:existing@example.com)";

        User existingUser = createCustomer();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                IllegalStateException.class,
                () -> authService.registerMerchantManager(
                        UUID.randomUUID(),
                        email,
                        "Jane",
                        "Smith",
                        "Password123!"
                )
        );

        verify(userRepository).findByEmail(email);

        verify(userRepository, never()).save(
                org.mockito.ArgumentMatchers.any()
        );

        verify(authIdentityRepository, never()).save(
                org.mockito.ArgumentMatchers.any()
        );

        verify(passwordHasher, never()).hash(
                org.mockito.ArgumentMatchers.any()
        );

        verifyNoInteractions(
                emailVerificationTokenGenerator,
                apiUrlProperties,
                emailSender
        );

    }

    @Test
    void shouldAuthenticateCustomer() {
        Customer customer = createCustomer();
        AuthIdentity identity = createEmailIdentity(customer.getId());

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(authIdentityRepository.findByUserIdAndProvider(
                customer.getId(),
                "email"
        )).thenReturn(Optional.of(identity));

        when(passwordHasher.matches(
                "Password123!",
                identity.getPasswordHash()
        )).thenReturn(true);

        User result = authService.authenticate(
                customer.getEmail(),
                "Password123!"
        );

        assertSame(customer, result);

        verify(userRepository).findByEmail(customer.getEmail());

        verify(authIdentityRepository)
                .findByUserIdAndProvider(
                        customer.getId(),
                        "email"
                );

        verify(passwordHasher).matches(
                "Password123!",
                identity.getPasswordHash()
        );

    }

    @Test
    void shouldAuthenticateMerchantManager() {
        MerchantManager manager = createMerchantManager();
        AuthIdentity identity = createEmailIdentity(manager.getId());

        when(userRepository.findByEmail(manager.getEmail()))
                .thenReturn(Optional.of(manager));

        when(authIdentityRepository.findByUserIdAndProvider(
                manager.getId(),
                "email"
        )).thenReturn(Optional.of(identity));

        when(passwordHasher.matches(
                "Password123!",
                identity.getPasswordHash()
        )).thenReturn(true);

        User result = authService.authenticate(
                manager.getEmail(),
                "Password123!"
        );

        assertSame(manager, result);

    }

    @Test
    void shouldRejectAuthenticationWhenEmailDoesNotExist() {
        String email = "[unknown@example.com](mailto:unknown@example.com)";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(
                        email,
                        "Password123!"
                )
        );

        verify(userRepository).findByEmail(email);

        verify(authIdentityRepository, never())
                .findByUserIdAndProvider(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );

        verify(passwordHasher, never()).matches(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );

    }

    @Test
    void shouldRejectAuthenticationWhenPasswordIsIncorrect() {
        Customer customer = createCustomer();
        AuthIdentity identity = createEmailIdentity(customer.getId());

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(authIdentityRepository.findByUserIdAndProvider(
                customer.getId(),
                "email"
        )).thenReturn(Optional.of(identity));

        when(passwordHasher.matches(
                "WrongPassword!",
                identity.getPasswordHash()
        )).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(
                        customer.getEmail(),
                        "WrongPassword!"
                )
        );

        verify(passwordHasher).matches(
                "WrongPassword!",
                identity.getPasswordHash()
        );

    }

    @Test
    void shouldRejectAuthenticationWhenIdentityDoesNotExist() {
        Customer customer = createCustomer();

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        when(authIdentityRepository.findByUserIdAndProvider(
                customer.getId(),
                "email"
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(
                        customer.getEmail(),
                        "Password123!"
                )
        );

        verify(passwordHasher, never()).matches(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );

    }

    @Test
    void shouldRejectAuthenticationWhenUserIsInactive() {
        Customer customer = new Customer(
                UUID.randomUUID(),
                "[inactive@example.com](mailto:inactive@example.com)",
                "John",
                "Doe",
                null,
                false,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        assertThrows(
                IllegalStateException.class,
                () -> authService.authenticate(
                        customer.getEmail(),
                        "Password123!"
                )
        );

        verify(authIdentityRepository, never())
                .findByUserIdAndProvider(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );

    }

    @Test
    void shouldRejectCustomerRegistrationWhenEmailIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.registerCustomer(
                        "   ",
                        "John",
                        "Doe",
                        null,
                        null,
                        "Password123!"
                )
        );

        verify(userRepository, never()).findByEmail(
                org.mockito.ArgumentMatchers.any()
        );

    }

    @Test
    void shouldRejectCustomerRegistrationWhenPasswordIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.registerCustomer(
                        "[customer@example.com](mailto:customer@example.com)",
                        "John",
                        "Doe",
                        null,
                        null,
                        "   "
                )
        );

        verify(userRepository, never()).findByEmail(
                org.mockito.ArgumentMatchers.any()
        );

    }

    @Test
    void shouldRejectMerchantRegistrationWhenMerchantIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.registerMerchantManager(
                        null,
                        "[manager@example.com](mailto:manager@example.com)",
                        "Jane",
                        "Smith",
                        "Password123!"
                )
        );

        verify(userRepository, never()).findByEmail(
                org.mockito.ArgumentMatchers.any()
        );

    }

    @Test
    void shouldLoginCustomer() {
        Customer customer = createCustomer();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        IRefreshSessionService.RefreshTokenCreationResult refreshResult =
                new IRefreshSessionService.RefreshTokenCreationResult(
                        null,
                        refreshToken
                );

        when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        AuthIdentity identity = createEmailIdentity(customer.getId());

        when(authIdentityRepository.findByUserIdAndProvider(
                customer.getId(),
                "email"
        )).thenReturn(Optional.of(identity));

        when(passwordHasher.matches(
                "Password123!",
                identity.getPasswordHash()
        )).thenReturn(true);

        when(accessTokenGenerator.generate(
                customer.getId(),
                "CUSTOMER"
        )).thenReturn(accessToken);

        when(refreshSessionService.createSession(customer.getId()))
                .thenReturn(refreshResult);

        IAuthService.AuthenticationResult result =
                authService.login(
                        customer.getEmail(),
                        "Password123!"
                );

        assertSame(customer, result.user());
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());

        verify(accessTokenGenerator).generate(
                customer.getId(),
                "CUSTOMER"
        );

        verify(refreshSessionService).createSession(
                customer.getId()
        );

    }

    @Test
    void shouldLoginMerchantManager() {
        MerchantManager manager = createMerchantManager();

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        IRefreshSessionService.RefreshTokenCreationResult refreshResult =
                new IRefreshSessionService.RefreshTokenCreationResult(
                        null,
                        refreshToken
                );

        when(userRepository.findByEmail(manager.getEmail()))
                .thenReturn(Optional.of(manager));

        AuthIdentity identity = createEmailIdentity(manager.getId());

        when(authIdentityRepository.findByUserIdAndProvider(
                manager.getId(),
                "email"
        )).thenReturn(Optional.of(identity));

        when(passwordHasher.matches(
                "Password123!",
                identity.getPasswordHash()
        )).thenReturn(true);

        when(accessTokenGenerator.generate(
                manager.getId(),
                "MERCHANT_MANAGER"
        )).thenReturn(accessToken);

        when(refreshSessionService.createSession(manager.getId()))
                .thenReturn(refreshResult);

        IAuthService.AuthenticationResult result =
                authService.login(
                        manager.getEmail(),
                        "Password123!"
                );

        assertSame(manager, result.user());
        assertEquals(accessToken, result.accessToken());
        assertEquals(refreshToken, result.refreshToken());

        verify(accessTokenGenerator).generate(
                manager.getId(),
                "MERCHANT_MANAGER"
        );

        verify(refreshSessionService).createSession(
                manager.getId()
        );

    }

    @Test
    void shouldRejectLoginWhenUserTypeIsUnsupported() {
        User unsupportedUser = new User(
                UUID.randomUUID(),
                "[unsupported@example.com](mailto:unsupported@example.com)",
                "John",
                "Doe",
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        ) {
        };

        AuthIdentity identity = createEmailIdentity(
                unsupportedUser.getId()
        );

        when(userRepository.findByEmail(
                unsupportedUser.getEmail()
        )).thenReturn(Optional.of(unsupportedUser));

        when(authIdentityRepository.findByUserIdAndProvider(
                unsupportedUser.getId(),
                "email"
        )).thenReturn(Optional.of(identity));

        when(passwordHasher.matches(
                "Password123!",
                identity.getPasswordHash()
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> authService.login(
                        unsupportedUser.getEmail(),
                        "Password123!"
                )
        );

        verifyNoInteractions(accessTokenGenerator);
        verifyNoInteractions(refreshSessionService);

    }

    @Test
    void shouldRefreshCustomerSession() {
        Customer customer = createCustomer();

        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        RefreshSession newSession = createRefreshSession(customer.getId());

        IRefreshSessionService.RefreshTokenRotationResult rotationResult =
                new IRefreshSessionService.RefreshTokenRotationResult(
                        createRefreshSession(customer.getId()),
                        newSession,
                        newRefreshToken
                );

        when(refreshSessionService.rotate(refreshToken))
                .thenReturn(rotationResult);

        when(userRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        when(accessTokenGenerator.generate(
                customer.getId(),
                "CUSTOMER"
        )).thenReturn(newAccessToken);

        IAuthService.AuthenticationResult result =
                authService.refresh(refreshToken);

        assertSame(customer, result.user());
        assertEquals(newAccessToken, result.accessToken());
        assertEquals(newRefreshToken, result.refreshToken());

        verify(refreshSessionService).rotate(refreshToken);
        verify(userRepository).findById(customer.getId());

        verify(accessTokenGenerator).generate(
                customer.getId(),
                "CUSTOMER"
        );

    }

    @Test
    void shouldRefreshMerchantManagerSession() {
        MerchantManager manager = createMerchantManager();

        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        RefreshSession newSession =
                createRefreshSession(manager.getId());

        IRefreshSessionService.RefreshTokenRotationResult rotationResult =
                new IRefreshSessionService.RefreshTokenRotationResult(
                        createRefreshSession(manager.getId()),
                        newSession,
                        newRefreshToken
                );

        when(refreshSessionService.rotate(refreshToken))
                .thenReturn(rotationResult);

        when(userRepository.findById(manager.getId()))
                .thenReturn(Optional.of(manager));

        when(accessTokenGenerator.generate(
                manager.getId(),
                "MERCHANT_MANAGER"
        )).thenReturn(newAccessToken);

        IAuthService.AuthenticationResult result =
                authService.refresh(refreshToken);

        assertSame(manager, result.user());
        assertEquals(newAccessToken, result.accessToken());
        assertEquals(newRefreshToken, result.refreshToken());

        verify(refreshSessionService).rotate(refreshToken);
        verify(userRepository).findById(manager.getId());

        verify(accessTokenGenerator).generate(
                manager.getId(),
                "MERCHANT_MANAGER"
        );

    }

    @Test
    void shouldRejectRefreshWhenTokenIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refresh(null)
        );

        verifyNoInteractions(
                refreshSessionService,
                userRepository,
                accessTokenGenerator
        );

    }

    @Test
    void shouldRejectRefreshWhenTokenIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refresh("   ")
        );

        verifyNoInteractions(
                refreshSessionService,
                userRepository,
                accessTokenGenerator
        );

    }

    @Test
    void shouldRejectRefreshWhenUserDoesNotExist() {
        Customer customer = createCustomer();

        String refreshToken = "refresh-token";

        RefreshSession newSession =
                createRefreshSession(customer.getId());

        IRefreshSessionService.RefreshTokenRotationResult rotationResult =
                new IRefreshSessionService.RefreshTokenRotationResult(
                        createRefreshSession(customer.getId()),
                        newSession,
                        "new-refresh-token"
                );

        when(refreshSessionService.rotate(refreshToken))
                .thenReturn(rotationResult);

        when(userRepository.findById(customer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> authService.refresh(refreshToken)
        );

        verify(refreshSessionService).rotate(refreshToken);
        verify(userRepository).findById(customer.getId());
        verifyNoInteractions(accessTokenGenerator);

    }

    @Test
    void shouldRejectRefreshWhenUserIsInactive() {
        Customer inactiveCustomer = new Customer(
                UUID.randomUUID(),
                "[inactive@example.com](mailto:inactive@example.com)",
                "John",
                "Doe",
                null,
                false,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        String refreshToken = "refresh-token";

        RefreshSession newSession =
                createRefreshSession(inactiveCustomer.getId());

        IRefreshSessionService.RefreshTokenRotationResult rotationResult =
                new IRefreshSessionService.RefreshTokenRotationResult(
                        createRefreshSession(inactiveCustomer.getId()),
                        newSession,
                        "new-refresh-token"
                );

        when(refreshSessionService.rotate(refreshToken))
                .thenReturn(rotationResult);

        when(userRepository.findById(inactiveCustomer.getId()))
                .thenReturn(Optional.of(inactiveCustomer));

        assertThrows(
                IllegalStateException.class,
                () -> authService.refresh(refreshToken)
        );

        verify(refreshSessionService).rotate(refreshToken);
        verify(userRepository).findById(inactiveCustomer.getId());
        verifyNoInteractions(accessTokenGenerator);

    }

    @Test
    void shouldRejectRefreshWhenUserTypeIsUnsupported() {
        User unsupportedUser = new User(
                UUID.randomUUID(),
                "[unsupported@example.com](mailto:unsupported@example.com)",
                "John",
                "Doe",
                false,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        ) {
        };

        String refreshToken = "refresh-token";

        RefreshSession newSession =
                createRefreshSession(unsupportedUser.getId());

        IRefreshSessionService.RefreshTokenRotationResult rotationResult =
                new IRefreshSessionService.RefreshTokenRotationResult(
                        createRefreshSession(unsupportedUser.getId()),
                        newSession,
                        "new-refresh-token"
                );

        when(refreshSessionService.rotate(refreshToken))
                .thenReturn(rotationResult);

        when(userRepository.findById(unsupportedUser.getId()))
                .thenReturn(Optional.of(unsupportedUser));

        assertThrows(
                IllegalStateException.class,
                () -> authService.refresh(refreshToken)
        );

        verify(refreshSessionService).rotate(refreshToken);
        verify(userRepository).findById(unsupportedUser.getId());
        verifyNoInteractions(accessTokenGenerator);

    }

    private Customer createCustomer() {
        LocalDateTime now = LocalDateTime.now();

        return new Customer(
                UUID.randomUUID(),
                "customer@example.com",
                "John",
                "Doe",
                "0612345678",
                true,
                true,
                LocalDate.of(1998, 5, 20),
                now,
                now
        );

    }

    private Customer createCustomerEmailNotVerified() {
        LocalDateTime now = LocalDateTime.now();

        return new Customer(
                UUID.randomUUID(),
                "customer@example.com",
                "John",
                "Doe",
                "0612345678",
                false,
                true,
                LocalDate.of(1998, 5, 20),
                now,
                now
        );

    }

    private MerchantManager createMerchantManager() {
        LocalDateTime now = LocalDateTime.now();

        return new MerchantManager(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "manager@example.com",
                "Jane",
                "Smith",
                true,
                true,
                now,
                now
        );

    }

    private MerchantManager createMerchantManagerEmailNotVerified() {
        LocalDateTime now = LocalDateTime.now();

        return new MerchantManager(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "manager@example.com",
                "Jane",
                "Smith",
                false,
                true,
                now,
                now
        );

    }

    private AuthIdentity createEmailIdentity(UUID userId) {
        return new AuthIdentity(
                UUID.randomUUID(),
                userId,
                "email",
                null,
                "hashed-password",
                LocalDateTime.now()
        );
    }

    private RefreshSession createRefreshSession(UUID userId) {
        LocalDateTime createdAt = LocalDateTime.now();

        return new RefreshSession(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "hashed-refresh-token",
                createdAt.plusDays(30),
                null,
                createdAt
        );

    }
}
