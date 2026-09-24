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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**

 * Service applicatif responsable de l'authentification
 * et de la gestion des comptes utilisateurs.
 *
 * <p>Ce service orchestre les opérations liées à l'inscription,
 * à l'authentification par e-mail et mot de passe ainsi qu'au
 * renouvellement des sessions d'authentification.</p>
 *
 * <p>La gestion HTTP des cookies et les détails techniques
 * liés à Spring Security restent en dehors de ce service.</p>
 */
@Service
public class AuthService implements IAuthService {

    private static final String EMAIL_PROVIDER = "email";
    private static final String CUSTOMER_ROLE = "CUSTOMER";
    private static final String MERCHANT_MANAGER_ROLE = "MERCHANT_MANAGER";

    private final IUserRepository userRepository;
    private final IAuthIdentityRepository authIdentityRepository;
    private final IPasswordHasher passwordHasher;
    private final IAccessTokenManagement accessTokenGenerator;
    private final IRefreshSessionService refreshSessionService;
    private final IEmailSender emailSender;
    private final IEmailVerificationTokenGenerator emailVerificationTokenGenerator;
    private final ApiUrlProperties apiUrlProperties;

    /**

     * Construit le service d'authentification.
     *
     * @param userRepository repository des utilisateurs
     * @param authIdentityRepository repository des identités d'authentification
     * @param passwordHasher composant de hachage et de vérification des mots de passe
     * @param accessTokenGenerator générateur des access tokens JWT
     * @param refreshSessionService service de gestion des refresh sessions
     * @param emailSender service d'envoi d'emails
     * @param emailVerificationTokenGenerator générateur des tokens de vérification email
     * @param apiUrlProperties propriétés de l'URL de base de l'API
     */
    public AuthService(
            IUserRepository userRepository,
            IAuthIdentityRepository authIdentityRepository,
            IPasswordHasher passwordHasher,
            IAccessTokenManagement accessTokenGenerator,
            IRefreshSessionService refreshSessionService,
            IEmailSender emailSender,
            IEmailVerificationTokenGenerator emailVerificationTokenGenerator,
            ApiUrlProperties apiUrlProperties
    ) {
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.passwordHasher = passwordHasher;
        this.accessTokenGenerator = accessTokenGenerator;
        this.refreshSessionService = refreshSessionService;
        this.emailSender = emailSender;
        this.emailVerificationTokenGenerator =
                emailVerificationTokenGenerator;
        this.apiUrlProperties = apiUrlProperties;
    }

    @Override
    @Transactional
    public Customer registerCustomer(
            String email,
            String firstName,
            String lastName,
            String phone,
            LocalDate birthDate,
            String password
    ) {
        validateRegistrationData(email, password);
        validateEmailIsAvailable(email);

  
        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer(
                UUID.randomUUID(),
                email,
                firstName,
                lastName,
                phone,
                false,
                true,
                birthDate,
                now,
                now
        );

        userRepository.save(customer);

        AuthIdentity identity = new AuthIdentity(
                UUID.randomUUID(),
                customer.getId(),
                EMAIL_PROVIDER,
                null,
                passwordHasher.hash(password),
                now
        );

        authIdentityRepository.save(identity);

        sendEmailVerification(customer);

        return customer;
  

    }

    @Override
    @Transactional
    public MerchantManager registerMerchantManager(
            UUID merchantId,
            String email,
            String firstName,
            String lastName,
            String password
    ) {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant id cannot be null");
        }

  
        validateRegistrationData(email, password);
        validateEmailIsAvailable(email);

        LocalDateTime now = LocalDateTime.now();

        MerchantManager merchantManager = new MerchantManager(
                UUID.randomUUID(),
                merchantId,
                email,
                firstName,
                lastName,
                false,
                true,
                now,
                now
        );

        userRepository.save(merchantManager);

        AuthIdentity identity = new AuthIdentity(
                UUID.randomUUID(),
                merchantManager.getId(),
                EMAIL_PROVIDER,
                null,
                passwordHasher.hash(password),
                now
        );

        authIdentityRepository.save(identity);

        sendEmailVerification(merchantManager);

        return merchantManager;
  

    }

    @Override
    public void verifyEmail(String token) {
        UUID userId =
                emailVerificationTokenGenerator.extractUserId(token);

  
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User associated with email verification token was not found"
                        ));

        if (!user.isEmailVerified()) {
            user.verifyEmail();
            userRepository.save(user);
        }
  

    }

    @Override
    public void resendVerificationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be null or blank"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));

        if (user.isEmailVerified()) {
            throw new IllegalStateException(
                    "Email address is already verified"
            );
        }

        sendEmailVerification(user);
    }

    @Override
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        ));

  
        if (!user.isActive()) {
            throw new IllegalStateException(
                    "User account is inactive"
            );
        }

        if (!user.isEmailVerified()) {
            throw new IllegalStateException(
                    "Email address is not verified"
            );
        }

        AuthIdentity identity = authIdentityRepository
                .findByUserIdAndProvider(user.getId(), EMAIL_PROVIDER)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        ));

        if (identity.getPasswordHash() == null
                || !passwordHasher.matches(
                password,
                identity.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        return user;
  

    }

    @Override
    @Transactional
    public AuthenticationResult login(
            String email,
            String password
    ) {
        User user = authenticate(email, password);

  
        String role = determineRole(user);

        String accessToken = accessTokenGenerator.generate(
                user.getId(),
                role
        );

        IRefreshSessionService.RefreshTokenCreationResult refreshResult =
                refreshSessionService.createSession(user.getId());

        return new AuthenticationResult(
                user,
                accessToken,
                refreshResult.refreshToken()
        );
  

    }

    @Override
    @Transactional
    public AuthenticationResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token cannot be null or blank"
            );
        }

  
        IRefreshSessionService.RefreshTokenRotationResult rotationResult =
                refreshSessionService.rotate(refreshToken);

        RefreshSession newSession = rotationResult.newSession();

        User user = userRepository.findById(newSession.getUserId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User associated with refresh session was not found"
                        ));

        if (!user.isActive()) {
            throw new IllegalStateException(
                    "User account is inactive"
            );
        }

        String role = determineRole(user);

        String accessToken = accessTokenGenerator.generate(
                user.getId(),
                role
        );

        return new AuthenticationResult(
                user,
                accessToken,
                rotationResult.refreshToken()
        );
  

    }

    private String determineRole(User user) {
        if (user instanceof Customer) {
            return CUSTOMER_ROLE;
        }

  
        if (user instanceof MerchantManager) {
            return MERCHANT_MANAGER_ROLE;
        }

        throw new IllegalStateException(
                "Unsupported user type for authentication"
        );
  

    }

    private void validateRegistrationData(
            String email,
            String password
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be null or blank"
            );
        }

  
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be null or blank"
            );
        }
  

    }

    private void validateEmailIsAvailable(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException(
                    "Email is already in use"
            );
        }
    }

    /**

     * Envoie l'email de vérification de l'adresse email.
     *
     * @param user utilisateur venant de s'inscrire
     */
    private void sendEmailVerification(User user) {
        String token = emailVerificationTokenGenerator.generate(
                user.getId()
        );

        String verificationLink =
                apiUrlProperties.getUrl()
                        + "auth/verify-email?token="
                        + token;

        emailSender.send(
                user.getEmail(),
                "Vérification de votre adresse email",
                """
                Bienvenue chez Fidely !
            
                
                     Merci de confirmer votre adresse email en cliquant sur le lien suivant :
            
                     %s
            
                     Ce lien permet de vérifier votre adresse email.
                """.formatted(verificationLink)
        );
    }
}
    